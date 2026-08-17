package io.java.pvz.net.server.handlers;

import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.users.User;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.ClientConnection;
import io.java.pvz.net.server.MatchRegistry;
import io.java.pvz.net.server.MatchSession;
import io.java.pvz.net.server.PlayerRole;
import io.java.pvz.net.server.RandomMatchQueue;
import io.java.pvz.net.server.SessionRegistry;
import io.java.pvz.net.server.game.MatchGameEngine;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MatchmakingHandler {

    private record PendingChallenge(String inviteId, ClientConnection inviter, ClientConnection target) {
    }

    private final SessionRegistry sessionRegistry;
    private final MatchRegistry matchRegistry;
    private final MatchGameEngine gameEngine;
    private final RandomMatchQueue randomQueue = new RandomMatchQueue();
    private final Map<String, PendingChallenge> pendingChallenges = new ConcurrentHashMap<>();

    public MatchmakingHandler(SessionRegistry sessionRegistry, MatchRegistry matchRegistry, MatchGameEngine gameEngine) {
        this.sessionRegistry = sessionRegistry;
        this.matchRegistry = matchRegistry;
        this.gameEngine = gameEngine;
    }

    public NetworkMessage checkUserOnline(NetworkMessage request, ClientConnection connection) {
        String username = request.getString("username");
        if (username == null || username.isBlank()) {
            return NetworkMessage.failure(request, "username is required");
        }
        username = username.trim();

        if (!DataBaseManager.usernameExists(username)) {
            return NetworkMessage.failure(request, "username does not exist");
        }

        NetworkMessage response = NetworkMessage.success(request);
        response.put("online", sessionRegistry.isOnline(username));
        return response;
    }

    public NetworkMessage challengeInvite(NetworkMessage request, ClientConnection connection) {
        User inviter = connection.getAuthenticatedUser();
        if (inviter == null) {
            return NetworkMessage.failure(request, "not logged in");
        }

        String targetUsername = request.getString("username");
        if (targetUsername == null || targetUsername.isBlank()) {
            return NetworkMessage.failure(request, "username is required");
        }
        targetUsername = targetUsername.trim();

        if (targetUsername.equalsIgnoreCase(inviter.getUsername())) {
            return NetworkMessage.failure(request, "you cannot challenge yourself");
        }

        if (!DataBaseManager.usernameExists(targetUsername)) {
            return NetworkMessage.failure(request, "username does not exist");
        }

        ClientConnection target = sessionRegistry.get(targetUsername);
        if (target == null) {
            return NetworkMessage.failure(request, "user is offline");
        }

        if (matchRegistry.getByConnection(connection) != null) {
            return NetworkMessage.failure(request, "you are already in a match");
        }
        if (matchRegistry.getByConnection(target) != null) {
            return NetworkMessage.failure(request, "that user is already in a match");
        }

        String inviteId = UUID.randomUUID().toString();
        pendingChallenges.put(inviteId, new PendingChallenge(inviteId, connection, target));

        NetworkMessage invitePush = NetworkMessage.request(MessageType.CHALLENGE_INVITE);
        invitePush.put("inviteId", inviteId);
        invitePush.put("fromUsername", inviter.getUsername());
        target.send(invitePush);

        NetworkMessage response = NetworkMessage.success(request);
        response.put("inviteId", inviteId);
        response.put("status", "sent");
        return response;
    }

    public NetworkMessage challengeResponse(NetworkMessage request, ClientConnection connection) {
        String inviteId = request.getString("inviteId");
        boolean accepted = Boolean.TRUE.equals(request.getBoolean("accepted"));

        PendingChallenge pending = (inviteId == null) ? null : pendingChallenges.remove(inviteId);
        if (pending == null) {
            return NetworkMessage.failure(request, "invite not found or expired");
        }
        if (pending.target() != connection) {
            return NetworkMessage.failure(request, "this invite is not addressed to you");
        }

        if (!accepted) {
            notifyInviterDeclined(pending, "declined");
            return NetworkMessage.success(request);
        }

        if (matchRegistry.getByConnection(pending.inviter()) != null
            || matchRegistry.getByConnection(connection) != null) {
            notifyInviterDeclined(pending, "one of the players already started another match");
            return NetworkMessage.failure(request, "one of the players already started another match");
        }

        MatchSession match = MatchSession.ofRandomRoles(pending.inviter(), connection);
        matchRegistry.register(match);
        announceMatchFound(match);

        return NetworkMessage.success(request);
    }

    public NetworkMessage queueJoinRandom(NetworkMessage request, ClientConnection connection) {
        if (connection.getAuthenticatedUser() == null) {
            return NetworkMessage.failure(request, "not logged in");
        }
        if (matchRegistry.getByConnection(connection) != null) {
            return NetworkMessage.failure(request, "you are already in a match");
        }

        MatchSession match = randomQueue.join(connection);
        NetworkMessage response = NetworkMessage.success(request);

        if (match == null) {
            response.put("status", "waiting");
            return response;
        }

        matchRegistry.register(match);
        announceMatchFound(match);
        response.put("status", "matched");
        response.put("matchId", match.getMatchId());
        return response;
    }

    public NetworkMessage queueLeave(NetworkMessage request, ClientConnection connection) {
        randomQueue.leave(connection);
        return NetworkMessage.success(request);
    }

    public void handleDisconnect(ClientConnection connection) {
        randomQueue.leave(connection);

        pendingChallenges.values().removeIf(pending -> {
            if (pending.inviter() == connection) {
                notifyInviterDisconnectToTarget(pending);
                return true;
            }
            if (pending.target() == connection) {
                notifyInviterDeclined(pending, "target disconnected");
                return true;
            }
            return false;
        });
    }

    private void notifyInviterDeclined(PendingChallenge pending, String reason) {
        NetworkMessage declinePush = NetworkMessage.request(MessageType.CHALLENGE_RESPONSE);
        declinePush.put("inviteId", pending.inviteId());
        declinePush.put("accepted", false);
        declinePush.put("reason", reason);
        pending.inviter().send(declinePush);
    }

    private void notifyInviterDisconnectToTarget(PendingChallenge pending) {
        NetworkMessage cancelPush = NetworkMessage.request(MessageType.CHALLENGE_RESPONSE);
        cancelPush.put("inviteId", pending.inviteId());
        cancelPush.put("accepted", false);
        cancelPush.put("reason", "inviter disconnected");
        pending.target().send(cancelPush);
    }

    private void announceMatchFound(MatchSession match) {
        gameEngine.startMatch(match);
        for (PlayerRole role : PlayerRole.values()) {
            ClientConnection conn = match.getConnection(role);
            if (conn == null) continue;

            ClientConnection opponentConn = match.getOpponentOf(conn);
            User opponentUser = opponentConn != null ? opponentConn.getAuthenticatedUser() : null;

            NetworkMessage push = NetworkMessage.request(MessageType.MATCH_FOUND);
            push.put("matchId", match.getMatchId());
            push.put("role", role.name());
            push.put("opponentUsername", opponentUser != null ? opponentUser.getUsername() : null);
            conn.send(push);
        }
    }
}
