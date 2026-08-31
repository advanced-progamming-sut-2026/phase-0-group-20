package io.java.pvz.controllers;

import com.badlogic.gdx.Gdx;
import io.java.pvz.net.client.NetworkClient;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;

import java.util.function.Consumer;

public class ProfileNetworkController {

    public void updateUsername(String newUsername, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.UPDATE_USERNAME);
        request.put("newUsername", newUsername);
        sendAsync(request, callback);
    }

    public void updateEmail(String newEmail, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.UPDATE_EMAIL);
        request.put("newEmail", newEmail);
        sendAsync(request, callback);
    }

    public void updateNickname(String newNickname, Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.UPDATE_NICKNAME);
        request.put("newNickname", newNickname);
        sendAsync(request, callback);
    }

    public void updatePassword(String oldPassword, String newPassword, String repeatPassword,
                               Consumer<NetworkMessage> callback) {
        NetworkMessage request = NetworkMessage.request(MessageType.UPDATE_PASSWORD);
        request.put("oldPassword", oldPassword);
        request.put("newPassword", newPassword);
        request.put("repeatPassword", repeatPassword);
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
        }, "profile-update-request");
        worker.setDaemon(true);
        worker.start();
    }
}
