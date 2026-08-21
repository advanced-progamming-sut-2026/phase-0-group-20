package io.java.pvz.controllers.GameController;

import com.badlogic.gdx.Gdx;
import io.java.pvz.net.client.NetworkClient;
import io.java.pvz.net.client.NetworkStateSyncer;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.PlayerRole;

import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.controllers.GameController.MiniGameController;
import io.java.pvz.models.game.GameSession;

public class MatchController {

    private static MatchController instance;
    private static boolean pushListenersRegistered = false;

    private Consumer<Map<String, Object>> onStateSync;
    private Consumer<NetworkMessage> onMatchEnd;
    private Consumer<NetworkMessage> onPauseStateChanged;

    private volatile String currentMatchId;
    private volatile PlayerRole currentRole;
    private volatile boolean isOnlineMatch = false;

    private volatile boolean isCouchPlay = false;

    public static synchronized MatchController getInstance() {
        if (instance == null) instance = new MatchController();
        return instance;
    }

    private MatchController() {
        registerPushListenersOnce();
    }

    private void registerPushListenersOnce() {
        if (pushListenersRegistered) return;
        pushListenersRegistered = true;

        NetworkClient client = NetworkClient.getInstance();

        client.onPush(MessageType.MATCH_STATE_SYNC, message -> Gdx.app.postRunnable(() -> {
            if (onStateSync != null) onStateSync.accept(message.getData());
            if (isOnlineMatch)
                NetworkStateSyncer.syncWithServer(message.getData());
        }));

        client.onPush(MessageType.MATCH_END, message -> Gdx.app.postRunnable(() -> {
            currentMatchId = null;
            isOnlineMatch = false;
            currentRole = null;
            if (onMatchEnd != null) onMatchEnd.accept(message);
        }));

        client.onPush(MessageType.MATCH_ACTION_BROADCAST, message -> Gdx.app.postRunnable(() -> {
            String action = message.getString("action");
            int col = message.getInt("col");
            int row = message.getInt("row");

            if ("PLACE_PLANT".equals(action)) {
                String plantName = message.getString("plantName");
                new GameFlowController().plantPlant(plantName, String.valueOf(col), String.valueOf(row));
            }
            else if ("RELEASE_ZOMBIE".equals(action)) {
                String zombieAlias = message.getString("zombieAlias");
                new MiniGameController().handlePutZombie(zombieAlias, String.valueOf(col), String.valueOf(row));
            }
            else if ("COLLECT_SUN".equals(action)) {
                new GameFlowController().collectSun(col, row);
            }
        }));

        client.onPush(MessageType.MATCH_PAUSE_STATE, message -> Gdx.app.postRunnable(() -> {
            boolean paused = Boolean.TRUE.equals(message.getBoolean("paused"));

            if (GameSession.getInstance() != null) {
                if (paused) {
                    GameSession.getInstance().pauseGame();
                } else {
                    GameSession.getInstance().resumeGame();
                }
            }

            if (onPauseStateChanged != null) onPauseStateChanged.accept(message);
        }));
    }

    public void setCurrentMatchId(String matchId) {
        this.currentMatchId = matchId;
    }

    public void setOnStateSync(Consumer<Map<String, Object>> listener) {
        this.onStateSync = listener;
    }

    public void setOnMatchEnd(Consumer<NetworkMessage> listener) {
        this.onMatchEnd = listener;
    }

    public void setOnPauseStateChanged(Consumer<NetworkMessage> listener) {
        this.onPauseStateChanged = listener;
    }

    public void placePlant(String plantName, int col, int row, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.MATCH_ACTION);
        request.put("action", "PLACE_PLANT");
        request.put("plantName", plantName);
        request.put("col", col);
        request.put("row", row);
        sendAsync(request, callback);
    }

    public void collectSun(int col, int row, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.MATCH_ACTION);
        request.put("action", "COLLECT_SUN");
        request.put("col", col);
        request.put("row", row);
        sendAsync(request, callback);
    }

    public void releaseZombie(String zombieAlias, int col, int row, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.MATCH_ACTION);
        request.put("action", "RELEASE_ZOMBIE");
        request.put("zombieAlias", zombieAlias);
        request.put("col", col);
        request.put("row", row);
        sendAsync(request, callback);
    }

    public void requestPause(Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.MATCH_PAUSE_REQUEST);
        sendAsync(request, callback);
    }

    public void requestResume(Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.MATCH_RESUME_REQUEST);
        sendAsync(request, callback);
    }

    private void sendAsync(NetworkMessage request, Consumer<NetworkMessage> callback) {
        Thread worker = new Thread(() -> {
            NetworkMessage response;
            try {
                response = NetworkClient.getInstance().sendAndWait(request, 10);
            } catch (TimeoutException e) {
                response = NetworkMessage.failure(request, "Server did not respond in time");
            } catch (Exception e) {
                response = NetworkMessage.failure(request, "Network error: " + e.getMessage());
            }
            NetworkMessage finalResponse = response;
            Gdx.app.postRunnable(() -> {
                if (callback != null) callback.accept(finalResponse);
            });
        }, "match-action-request");
        worker.setDaemon(true);
        worker.start();
    }

    public void surrender(Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.MATCH_SURRENDER);
        sendAsync(request, callback);
    }

    public boolean isOnlineMatch() {
        return isOnlineMatch;
    }

    public io.java.pvz.net.server.PlayerRole getCurrentRole() {
        return currentRole;
    }

    public boolean isCouchPlay() {
        return isCouchPlay;
    }

    public void setCouchPlay(boolean couchPlay) {
        this.isCouchPlay = couchPlay;
    }

    public void setOnlineMatch(boolean onlineMatch) {
        this.isOnlineMatch = onlineMatch;
    }

    public void setupOnlineMatch(String matchId, PlayerRole role) {
        this.currentMatchId = matchId;
        this.currentRole = role;
        this.isOnlineMatch = true;
        this.isCouchPlay = false;
    }
}
