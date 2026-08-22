package io.java.pvz.controllers.GameController;

import com.badlogic.gdx.Gdx;
import io.java.pvz.net.client.NetworkClient;
import io.java.pvz.net.client.ServerConfig;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NetworkController {

    private static NetworkController instance;

    private volatile boolean authenticated = false;
    private volatile String authenticatedUsername;

    public static synchronized NetworkController getInstance() {
        if (instance == null) instance = new NetworkController();
        return instance;
    }

    private NetworkController() {
    }

    public boolean isConnected() {
        return NetworkClient.getInstance().isConnected();
    }

    public boolean isAuthenticated() {
        return authenticated && isConnected();
    }

    public void login(String username, String password, Consumer<NetworkMessage> callback) {
        runAsync(() -> {
            NetworkClient client = connectIfNeeded();
            NetworkMessage request = NetworkMessage.request(MessageType.LOGIN);
            request.put("username", username);
            request.put("password", password);
            request.put("stayLoggedIn", false);
            return client.sendAndWait(request, 10);
        }, response -> {
            if (response != null && response.isSuccess()) {
                authenticated = true;
                authenticatedUsername = username;
            }
            if (callback != null) callback.accept(response);
        });
    }

    public void register(String username, String password, String repeatPassword, String nickname,
                         String email, String gender, Consumer<NetworkMessage> callback) {
        sendAsync(() -> {
            NetworkMessage request = NetworkMessage.request(MessageType.SIGNUP_REGISTER);
            request.put("username", username);
            request.put("password", password);
            request.put("repeatPassword", repeatPassword);
            request.put("nickname", nickname);
            request.put("email", email);
            request.put("gender", gender);
            return request;
        }, callback);
    }

    public void pickSecurityQuestion(String questionNumber, String answer, String confirmAnswer,
                                     Consumer<NetworkMessage> callback) {
        sendAsync(() -> {
            NetworkMessage request = NetworkMessage.request(MessageType.SIGNUP_PICK_QUESTION);
            request.put("questionNumber", questionNumber);
            request.put("answer", answer);
            request.put("confirmAnswer", confirmAnswer);
            return request;
        }, callback);
    }

    public void logout(Consumer<NetworkMessage> callback) {
        boolean wasConnected = isConnected();
        authenticated = false;
        authenticatedUsername = null;

        if (!wasConnected) {
            if (callback != null) callback.accept(null);
            return;
        }
        sendAsync(() -> NetworkMessage.request(MessageType.LOGOUT), callback);
    }

    private NetworkClient connectIfNeeded() throws Exception {
        NetworkClient client = NetworkClient.getInstance();
        if (!client.isConnected()) {
            client.connect(ServerConfig.DEFAULT_HOST, ServerConfig.DEFAULT_PORT);
        }
        return client;
    }

    private void sendAsync(Supplier<NetworkMessage> requestSupplier, Consumer<NetworkMessage> callback) {
        runAsync(() -> connectIfNeeded().sendAndWait(requestSupplier.get(), 10), callback);
    }

    private interface NetworkCall {
        NetworkMessage run() throws Exception;
    }

    private void runAsync(NetworkCall call, Consumer<NetworkMessage> callback) {
        Thread worker = new Thread(() -> {
            NetworkMessage response;
            try {
                response = call.run();
            } catch (Exception e) {
                NetworkMessage placeholder = NetworkMessage.request(MessageType.ERROR);
                response = NetworkMessage.failure(placeholder, "Network error: " + e.getMessage());
            }
            NetworkMessage finalResponse = response;
            Gdx.app.postRunnable(() -> {
                if (callback != null) callback.accept(finalResponse);
            });
        }, "network-session-request");
        worker.setDaemon(true);
        worker.start();
    }
}
