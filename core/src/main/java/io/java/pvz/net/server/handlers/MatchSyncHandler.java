package io.java.pvz.net.server.handlers;

import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.ClientConnection;
import io.java.pvz.net.server.MatchRegistry;
import io.java.pvz.net.server.MatchSession;
import io.java.pvz.net.server.game.MatchGameEngine;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MatchSyncHandler {

    private final MatchRegistry matchRegistry;
    private final MatchGameEngine gameEngine;

    public MatchSyncHandler(MatchRegistry matchRegistry, MatchGameEngine gameEngine) {
        this.matchRegistry = matchRegistry;
        this.gameEngine = gameEngine;
    }

    public NetworkMessage handleAction(NetworkMessage request, ClientConnection connection) {
        if (connection.getAuthenticatedUser() == null) {
            return NetworkMessage.failure(request, "not logged in");
        }

        MatchSession match = matchRegistry.getByConnection(connection);
        if (match == null) {
            return NetworkMessage.failure(request, "you are not currently in a match");
        }

        CompletableFuture<Boolean> success = new CompletableFuture<>();
        CompletableFuture<String> message = new CompletableFuture<>();

        gameEngine.applyAction(match.getMatchId(), connection, request, (ok, msg) -> {
            success.complete(ok);
            message.complete(msg);
        });

        try {
            boolean ok = success.get(5, TimeUnit.SECONDS);
            String msg = message.get(5, TimeUnit.SECONDS);
            return ok ? NetworkMessage.success(request) : NetworkMessage.failure(request, msg);
        } catch (TimeoutException e) {
            return NetworkMessage.failure(request, "match engine timed out");
        } catch (Exception e) {
            return NetworkMessage.failure(request, "internal error: " + e.getMessage());
        }
    }
}
