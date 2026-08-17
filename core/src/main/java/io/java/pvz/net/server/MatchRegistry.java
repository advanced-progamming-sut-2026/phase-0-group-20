package io.java.pvz.net.server;

import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class MatchRegistry {

    private final Map<String, MatchSession> matchesById = new ConcurrentHashMap<>();
    private final Map<String, MatchSession> matchByUsername = new ConcurrentHashMap<>();
    private volatile Consumer<MatchSession> onEnd;

    public void setOnEnd(Consumer<MatchSession> onEnd) {
        this.onEnd = onEnd;
    }

    public MatchSession register(MatchSession match) {
        matchesById.put(match.getMatchId(), match);
        for (PlayerRole role : PlayerRole.values()) {
            ClientConnection conn = match.getConnection(role);
            if (conn != null && conn.getAuthenticatedUser() != null) {
                matchByUsername.put(conn.getAuthenticatedUser().getUsername(), match);
            }
        }
        return match;
    }

    public MatchSession getById(String matchId) {
        return matchesById.get(matchId);
    }

    public MatchSession getByUsername(String username) {
        return username == null ? null : matchByUsername.get(username);
    }

    public MatchSession getByConnection(ClientConnection connection) {
        if (connection == null || connection.getAuthenticatedUser() == null) return null;
        return matchByUsername.get(connection.getAuthenticatedUser().getUsername());
    }

    public void end(MatchSession match) {
        if (match == null) return;
        match.finish();
        matchesById.remove(match.getMatchId());
        for (PlayerRole role : PlayerRole.values()) {
            ClientConnection conn = match.getConnection(role);
            if (conn != null && conn.getAuthenticatedUser() != null) {
                matchByUsername.remove(conn.getAuthenticatedUser().getUsername());
            }
        }
        if (onEnd != null) onEnd.accept(match);
    }

    public void handleDisconnect(ClientConnection connection) {
        MatchSession match = getByConnection(connection);
        if (match == null) return;

        ClientConnection opponent = match.getOpponentOf(connection);
        if (opponent != null) {
            NetworkMessage notice = NetworkMessage.request(MessageType.MATCH_END);
            notice.put("matchId", match.getMatchId());
            notice.put("reason", "opponent_disconnected");
            opponent.send(notice);
        }
        end(match);
    }
}
