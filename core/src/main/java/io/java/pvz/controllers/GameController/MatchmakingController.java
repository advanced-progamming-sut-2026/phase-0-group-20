package io.java.pvz.controllers.GameController;

import com.badlogic.gdx.Gdx;
import io.java.pvz.net.client.NetworkClient;
import io.java.pvz.net.client.ServerConfig;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;

import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class MatchmakingController {

    public static class MatchFoundInfo {
        public final String matchId;
        public final String role;
        public final String opponentUsername;

        public MatchFoundInfo(String matchId, String role, String opponentUsername) {
            this.matchId = matchId;
            this.role = role;
            this.opponentUsername = opponentUsername;
        }
    }

    public static class IncomingChallenge {
        public final String inviteId;
        public final String fromUsername;

        public IncomingChallenge(String inviteId, String fromUsername) {
            this.inviteId = inviteId;
            this.fromUsername = fromUsername;
        }
    }

    private static MatchmakingController instance;
    private static boolean pushListenersRegistered = false;

    private Consumer<IncomingChallenge> onIncomingChallenge;
    private Consumer<String> onChallengeDeclined;
    private Consumer<MatchFoundInfo> onMatchFound;

    public static synchronized MatchmakingController getInstance() {
        if (instance == null) instance = new MatchmakingController();
        return instance;
    }

    private MatchmakingController() {
        registerPushListenersOnce();
    }

    private void registerPushListenersOnce() {
        if (pushListenersRegistered) return;
        pushListenersRegistered = true;

        NetworkClient client = NetworkClient.getInstance();

        client.onPush(MessageType.CHALLENGE_INVITE, message -> Gdx.app.postRunnable(() -> {
            if (onIncomingChallenge != null) {
                onIncomingChallenge.accept(new IncomingChallenge(
                    message.getString("inviteId"), message.getString("fromUsername")));
            }
        }));

        client.onPush(MessageType.CHALLENGE_RESPONSE, message -> Gdx.app.postRunnable(() -> {
            if (onChallengeDeclined != null) {
                String reason = message.getString("reason");
                onChallengeDeclined.accept(reason != null ? reason : "declined");
            }
        }));

        client.onPush(MessageType.MATCH_FOUND, message -> Gdx.app.postRunnable(() -> {
            if (onMatchFound != null) {
                onMatchFound.accept(new MatchFoundInfo(
                    message.getString("matchId"), message.getString("role"), message.getString("opponentUsername")));
            }
        }));
    }

    public void setOnIncomingChallenge(Consumer<IncomingChallenge> listener) {
        this.onIncomingChallenge = listener;
    }

    public void setOnChallengeDeclined(Consumer<String> listener) {
        this.onChallengeDeclined = listener;
    }

    public void setOnMatchFound(Consumer<MatchFoundInfo> listener) {
        this.onMatchFound = listener;
    }

    public void sendChallenge(String username, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.CHALLENGE_INVITE);
        request.put("username", username);
        sendAsync(request, callback);
    }

    public void respondToChallenge(String inviteId, boolean accepted, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.CHALLENGE_RESPONSE);
        request.put("inviteId", inviteId);
        request.put("accepted", accepted);
        sendAsync(request, callback);
    }

    public void joinRandomQueue(Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.QUEUE_JOIN_RANDOM);
        sendAsync(request, callback);
    }

    public void leaveRandomQueue(Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.QUEUE_LEAVE);
        sendAsync(request, callback);
    }

    private void sendAsync(NetworkMessage request, Consumer<NetworkMessage> callback) {
        runAsync(() -> NetworkClient.getInstance().sendAndWait(request, 10), callback);
    }

    private interface NetworkCall {
        NetworkMessage run() throws Exception;
    }

    private void runAsync(NetworkCall call, Consumer<NetworkMessage> callback) {
        Thread worker = new Thread(() -> {
            NetworkMessage response;
            try {
                response = call.run();
            } catch (TimeoutException e) {
                response = failure("Server did not respond in time");
            } catch (Exception e) {
                response = failure("Network error: " + e.getMessage());
            }
            NetworkMessage finalResponse = response;
            Gdx.app.postRunnable(() -> {
                if (callback != null) callback.accept(finalResponse);
            });
        }, "matchmaking-request");
        worker.setDaemon(true);
        worker.start();
    }

    private NetworkMessage failure(String message) {
        NetworkMessage placeholder = NetworkMessage.request(MessageType.ERROR);
        NetworkMessage response = NetworkMessage.failure(placeholder, message);
        return response;
    }
}
