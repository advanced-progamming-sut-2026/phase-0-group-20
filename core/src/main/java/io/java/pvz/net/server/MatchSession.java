package io.java.pvz.net.server;

import io.java.pvz.net.protocol.NetworkMessage;

import java.util.EnumMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class MatchSession {

    public enum Status {
        WAITING_FOR_OPPONENT,
        ACTIVE,
        FINISHED
    }

    private final String matchId;
    private final Map<PlayerRole, ClientConnection> players = new EnumMap<>(PlayerRole.class);
    private volatile Status status;

    private MatchSession() {
        this.matchId = UUID.randomUUID().toString();
        this.status = Status.WAITING_FOR_OPPONENT;
    }

    public static MatchSession of(ClientConnection first, ClientConnection second, boolean firstIsPlant) {
        MatchSession match = new MatchSession();
        match.players.put(firstIsPlant ? PlayerRole.PLANT : PlayerRole.ZOMBIE, first);
        match.players.put(firstIsPlant ? PlayerRole.ZOMBIE : PlayerRole.PLANT, second);
        match.status = Status.ACTIVE;
        return match;
    }

    public static MatchSession ofRandomRoles(ClientConnection a, ClientConnection b) {
        boolean aIsPlant = new Random().nextBoolean();
        return of(a, b, aIsPlant);
    }

    public String getMatchId() {
        return matchId;
    }

    public Status getStatus() {
        return status;
    }

    public void finish() {
        this.status = Status.FINISHED;
    }

    public ClientConnection getConnection(PlayerRole role) {
        return players.get(role);
    }

    public PlayerRole getRoleOf(ClientConnection connection) {
        for (Map.Entry<PlayerRole, ClientConnection> entry : players.entrySet()) {
            if (entry.getValue() == connection) return entry.getKey();
        }
        return null;
    }

    public ClientConnection getOpponentOf(ClientConnection connection) {
        PlayerRole role = getRoleOf(connection);
        if (role == null) return null;
        PlayerRole opponentRole = (role == PlayerRole.PLANT) ? PlayerRole.ZOMBIE : PlayerRole.PLANT;
        return players.get(opponentRole);
    }

    public boolean involves(ClientConnection connection) {
        return players.containsValue(connection);
    }

    public void relayToOpponent(ClientConnection sender, NetworkMessage message) {
        ClientConnection opponent = getOpponentOf(sender);
        if (opponent != null) opponent.send(message);
    }
}
