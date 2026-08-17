package io.java.pvz.net.server.handlers;

import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.ClientConnection;
import io.java.pvz.net.server.MatchRegistry;
import io.java.pvz.net.server.MatchSession;

import java.util.Set;

public class ReactionHandler {

    private static final Set<String> VALID_CATEGORIES = Set.of("TEXT", "EMOJI", "STICKER");

    private final MatchRegistry matchRegistry;

    public ReactionHandler(MatchRegistry matchRegistry) {
        this.matchRegistry = matchRegistry;
    }

    public NetworkMessage handleReactionSend(NetworkMessage request, ClientConnection connection) {
        if (connection.getAuthenticatedUser() == null) {
            return NetworkMessage.failure(request, "not logged in");
        }

        String category = request.getString("category");
        Integer index = request.getInt("index");

        if (category == null || !VALID_CATEGORIES.contains(category.toUpperCase())) {
            return NetworkMessage.failure(request, "invalid reaction category");
        }
        if (index == null || index < 0 || index > 2) {
            return NetworkMessage.failure(request, "reaction index must be between 0 and 2");
        }

        MatchSession match = matchRegistry.getByConnection(connection);
        if (match == null) {
            return NetworkMessage.failure(request, "you are not currently in a match");
        }

        ClientConnection opponent = match.getOpponentOf(connection);
        if (opponent == null) {
            return NetworkMessage.failure(request, "no opponent to send this to");
        }

        NetworkMessage push = NetworkMessage.request(MessageType.REACTION_SEND);
        push.put("matchId", match.getMatchId());
        push.put("fromUsername", connection.getAuthenticatedUser().getUsername());
        push.put("category", category.toUpperCase());
        push.put("index", index);
        opponent.send(push);

        return NetworkMessage.success(request);
    }
}
