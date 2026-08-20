package io.java.pvz.net.server.game;

import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.controllers.GameController.MiniGameController;
import io.java.pvz.models.Result;
import io.java.pvz.models.enums.GameState;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.game.minigame.IZombieLevel;
import io.java.pvz.models.game.minigame.MiniGameFactory;
import io.java.pvz.models.game.minigame.MiniGameType;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.ClientConnection;
import io.java.pvz.net.server.MatchRegistry;
import io.java.pvz.net.server.MatchSession;
import io.java.pvz.net.server.PlayerRole;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MatchGameEngine {

    private static final int TICK_INTERVAL_MS = 100;
    private static final int TICKS_PER_INTERVAL = 6;

    private final ScheduledExecutorService executor =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "match-game-engine");
            t.setDaemon(true);
            return t;
        });

    private final Map<String, NetworkMatchState> matches = new ConcurrentHashMap<>();
    private final MatchRegistry matchRegistry;
    private final Random random = new Random();

    public MatchGameEngine(MatchRegistry matchRegistry) {
        this.matchRegistry = matchRegistry;
    }

    public void startMatch(MatchSession matchSession) {
        executor.submit(() -> {
            try {
                int levelNumber = random.nextInt(3) + 1;
                IZombieLevel level = (IZombieLevel) MiniGameFactory.createLevel(MiniGameType.I_ZOMBIE, levelNumber);

                NetworkMatchState match = new NetworkMatchState(matchSession, level);
                matches.put(match.getMatchId(), match);

                broadcast(match, MatchStateSnapshotBuilder.build(match));

                match.setTickTask(executor.scheduleAtFixedRate(
                    () -> tick(match), TICK_INTERVAL_MS, TICK_INTERVAL_MS, TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                System.err.println("Failed to start match " + matchSession.getMatchId() + ": " + e);
                e.printStackTrace();
            }
        });
    }

    private void tick(NetworkMatchState match) {
        if (match.isEnded()) return;
        try {
            match.activate();
            GameSession session = match.getGameSession();
            session.update(TICKS_PER_INTERVAL);

            broadcast(match, MatchStateSnapshotBuilder.build(match));

            if (session.isGameOver()) {
                finishMatch(match, session.getState());
            }
        } catch (Exception e) {
            System.err.println("Error ticking match " + match.getMatchId() + ": " + e);
            e.printStackTrace();
        }
    }

    public void applyAction(String matchId, ClientConnection sender, NetworkMessage request,
                            ActionResultCallback callback) {
        NetworkMatchState match = matches.get(matchId);
        if (match == null) {
            callback.onResult(false, "match not found or already finished");
            return;
        }

        executor.submit(() -> {
            try {
                match.activate();
                Result result = doApplyAction(match, sender, request);
                callback.onResult(result.isSuccessful(), result.message());

                if (result.isSuccessful()) {
                    NetworkMessage broadcast = NetworkMessage.request(MessageType.MATCH_ACTION_BROADCAST);
                    broadcast.put("action", request.getString("action"));
                    broadcast.put("plantName", request.getString("plantName"));
                    broadcast.put("zombieAlias", request.getString("zombieAlias"));
                    broadcast.put("col", request.getInt("col"));
                    broadcast.put("row", request.getInt("row"));

                    ClientConnection plantConn = match.getPlantConnection();
                    ClientConnection zombieConn = match.getZombieConnection();
                    if (plantConn != null) plantConn.send(broadcast);
                    if (zombieConn != null) zombieConn.send(broadcast);

                    broadcast(match, MatchStateSnapshotBuilder.build(match));
                }

                GameSession session = match.getGameSession();
                if (session.isGameOver() && !match.isEnded()) {
                    finishMatch(match, session.getState());
                }
            } catch (Exception e) {
                System.err.println("Error applying action for match " + matchId + ": " + e);
                e.printStackTrace();
                callback.onResult(false, "internal error: " + e.getMessage());
            }
        });
    }

    private Result doApplyAction(NetworkMatchState match, ClientConnection sender, NetworkMessage request) {
        PlayerRole role = match.getMatchSession().getRoleOf(sender);
        if (role == null) return new Result(false, "you are not part of this match");

        String action = request.getString("action");
        if (action == null) return new Result(false, "missing action");

        IZombieLevel level = match.getLevel();

        switch (action) {
            case "PLACE_PLANT" -> {
                if (role != PlayerRole.PLANT) return new Result(false, "only the plant side can place plants");
                Integer col = request.getInt("col");
                if (col != null && !(col < level.getRedLineCol())) {
                    return new Result(false, "you can only plant behind the red line (col < "
                        + (level.getRedLineCol() + 1) + ")");
                }
                return new GameFlowController().plantPlant(
                    request.getString("plantName"), request.getString("col"), request.getString("row"));
            }
            case "COLLECT_SUN" -> {
                if (role != PlayerRole.ZOMBIE) {
                    GameSession.notify("only zombie can collect sun");
                    return new Result(false, "only the zombie side can collect sun");
                }
                Integer col = request.getInt("col");
                Integer row = request.getInt("row");
                if (col == null || row == null) return new Result(false, "missing col/row");
                return new GameFlowController().collectSun(col, row);
            }
            case "RELEASE_ZOMBIE" -> {
                if (role != PlayerRole.ZOMBIE) return new Result(false, "only the zombie side can release zombies");
                return new MiniGameController().handlePutZombie(
                    request.getString("zombieAlias"), request.getString("col"), request.getString("row"));
            }
            default -> {
                return new Result(false, "unsupported action: " + action);
            }
        }
    }

    private void finishMatch(NetworkMatchState match, GameState finalState) {
        PlayerRole winner = (finalState == GameState.WON) ? PlayerRole.ZOMBIE : PlayerRole.PLANT;
        finishMatch(match, winner, "finished");
    }

    private void finishMatch(NetworkMatchState match, PlayerRole winner, String reason) {
        if (match.isEnded()) return;
        match.markEnded();

        NetworkMessage endMsg = NetworkMessage.request(MessageType.MATCH_END);
        endMsg.put("matchId", match.getMatchId());
        endMsg.put("reason", reason);
        endMsg.put("winnerRole", winner.name());
        endMsg.put("winnerUsername", winner == PlayerRole.PLANT
            ? match.getPlantUser().getUsername() : match.getZombieUser().getUsername());

        ClientConnection plantConn = match.getPlantConnection();
        ClientConnection zombieConn = match.getZombieConnection();
        if (plantConn != null) plantConn.send(endMsg);
        if (zombieConn != null) zombieConn.send(endMsg);

        matches.remove(match.getMatchId());
        matchRegistry.end(match.getMatchSession());
    }

    public void cancelMatch(String matchId) {
        NetworkMatchState match = matches.remove(matchId);
        if (match != null) match.markEnded();
    }

    private void broadcast(NetworkMatchState match, Map<String, Object> snapshotData) {
        NetworkMessage push = NetworkMessage.request(MessageType.MATCH_STATE_SYNC);
        push.getData().putAll(snapshotData);

        ClientConnection plantConn = match.getPlantConnection();
        ClientConnection zombieConn = match.getZombieConnection();
        if (plantConn != null) plantConn.send(push);
        if (zombieConn != null) zombieConn.send(push);
    }

    @FunctionalInterface
    public interface ActionResultCallback {
        void onResult(boolean success, String message);
    }

    public void handlePlayerLeft(String matchId, ClientConnection leaver, String reason) {
        executor.submit(() -> {
            NetworkMatchState match = matches.get(matchId);
            if (match == null || match.isEnded()) return;

            PlayerRole leaverRole = match.getMatchSession().getRoleOf(leaver);
            if (leaverRole == null) return;

            PlayerRole winner = (leaverRole == PlayerRole.PLANT) ? PlayerRole.ZOMBIE : PlayerRole.PLANT;
            finishMatch(match, winner, reason);
        });
    }

}
