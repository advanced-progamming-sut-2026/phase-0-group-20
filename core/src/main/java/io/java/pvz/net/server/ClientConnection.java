package io.java.pvz.net.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.java.pvz.controllers.MenuController.LoginMenuController;
import io.java.pvz.controllers.MenuController.SignupMenuController;
import io.java.pvz.models.users.User;
import io.java.pvz.net.protocol.NetworkMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientConnection implements Runnable {

    private final Socket socket;
    private final GameServer server;
    private final ObjectMapper mapper = new ObjectMapper();

    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;

    private User authenticatedUser;

    private final SignupMenuController signupController = new SignupMenuController();
    private final LoginMenuController loginController = new LoginMenuController();

    public ClientConnection(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            String line;
            while (running && (line = in.readLine()) != null) {
                if (line.isBlank()) continue;

                NetworkMessage request;
                try {
                    request = mapper.readValue(line, NetworkMessage.class);
                } catch (Exception e) {
                    System.err.println("Malformed message from " + describe() + ": " + e.getMessage());
                    continue;
                }

                NetworkMessage response = server.getDispatcher().dispatch(request, this);
                if (response != null) send(response);
            }
        } catch (IOException e) {
            System.out.println(describe() + " disconnected (" + e.getMessage() + ")");
        } finally {
            close();
        }
    }

    public synchronized void send(NetworkMessage message) {
        if (out == null) return;
        try {
            out.println(mapper.writeValueAsString(message));
        } catch (Exception e) {
            System.err.println("Failed to send to " + describe() + ": " + e.getMessage());
        }
    }

    public void close() {
        if (!running) return;
        running = false;
        if (authenticatedUser != null) {
            server.getSessionRegistry().unregister(authenticatedUser.getUsername());
        }
        server.getMatchRegistry().handleDisconnect(this);
        server.getMatchmakingHandler().handleDisconnect(this);
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        server.removeConnection(this);
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(User user) {
        if (authenticatedUser != null) {
            server.getSessionRegistry().unregister(authenticatedUser.getUsername());
        }
        this.authenticatedUser = user;
        if (user != null) {
            server.getSessionRegistry().register(user.getUsername(), this);
        }
    }

    public SignupMenuController getSignupController() {
        return signupController;
    }

    public LoginMenuController getLoginController() {
        return loginController;
    }

    private String describe() {
        return authenticatedUser != null
            ? "client[" + authenticatedUser.getUsername() + "]"
            : "client[" + socket.getRemoteSocketAddress() + "]";
    }
}
