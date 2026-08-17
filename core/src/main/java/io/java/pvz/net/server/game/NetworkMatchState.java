package io.java.pvz.net.server.game;

import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.minigame.IZombieLevel;
import io.java.pvz.models.users.User;
import io.java.pvz.net.server.ClientConnection;
import io.java.pvz.net.server.MatchSession;
import io.java.pvz.net.server.PlayerRole;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class NetworkMatchState {

    private final MatchSession matchSession;
    private final User plantUser;
    private final User zombieUser;
    private final GameSession gameSession;
    private final IZombieLevel level;

    private volatile ScheduledFuture<?> tickTask;
    private volatile boolean ended = false;

    public NetworkMatchState(MatchSession matchSession, IZombieLevel level) {
        this.matchSession = matchSession;
        this.level = level;

        ClientConnection plantConn = matchSession.getConnection(PlayerRole.PLANT);
        ClientConnection zombieConn = matchSession.getConnection(PlayerRole.ZOMBIE);
        this.plantUser = plantConn.getAuthenticatedUser();
        this.zombieUser = zombieConn.getAuthenticatedUser();

        List<Plant> plantDeck = new ArrayList<>(plantUser.getUnlockedPlants());

        App.setActiveUser(plantUser);
        GameSession.startMiniGame(level, plantDeck);
        this.gameSession = GameSession.getInstance();
    }

    public void activate() {
        App.setActiveUser(plantUser);
        GameSession.bindInstance(gameSession);
    }

    public MatchSession getMatchSession() {
        return matchSession;
    }

    public GameSession getGameSession() {
        return gameSession;
    }

    public IZombieLevel getLevel() {
        return level;
    }

    public User getPlantUser() {
        return plantUser;
    }

    public User getZombieUser() {
        return zombieUser;
    }

    public ClientConnection getPlantConnection() {
        return matchSession.getConnection(PlayerRole.PLANT);
    }

    public ClientConnection getZombieConnection() {
        return matchSession.getConnection(PlayerRole.ZOMBIE);
    }

    public String getMatchId() {
        return matchSession.getMatchId();
    }

    public void setTickTask(ScheduledFuture<?> tickTask) {
        this.tickTask = tickTask;
    }

    public boolean isEnded() {
        return ended;
    }

    public void markEnded() {
        this.ended = true;
        if (tickTask != null) tickTask.cancel(false);
    }
}
