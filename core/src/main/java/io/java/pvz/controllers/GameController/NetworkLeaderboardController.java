package io.java.pvz.controllers.GameController;

import com.badlogic.gdx.Gdx;
import io.java.pvz.net.client.NetworkClient;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;

import java.util.function.Consumer;

public class NetworkLeaderboardController {

    public void fetchLeaderboard(String sortType, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.LEADERBOARD_REQUEST);
        request.put("sortType", sortType);
        sendAsync(request, callback);
    }

    public void submitScore(int score, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.SCORE_SUBMIT);
        request.put("score", score);
        sendAsync(request, callback);
    }

    private void sendAsync(NetworkMessage request, Consumer<NetworkMessage> callback) {
        Thread worker = new Thread(() -> {
            NetworkMessage response;
            try {
                response = NetworkClient.getInstance().sendAndWait(request, 10);
            } catch (Exception e) {
                response = NetworkMessage.failure(request, "Network error: " + e.getMessage());
            }
            NetworkMessage finalResponse = response;
            Gdx.app.postRunnable(() -> {
                if (callback != null) callback.accept(finalResponse);
            });
        }, "leaderboard-request");
        worker.setDaemon(true);
        worker.start();
    }
}
