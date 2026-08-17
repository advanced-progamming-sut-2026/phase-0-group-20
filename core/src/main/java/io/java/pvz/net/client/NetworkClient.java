package io.java.pvz.net.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class NetworkClient {

    private static NetworkClient instance;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, CompletableFuture<NetworkMessage>> pending = new ConcurrentHashMap<>();
    private final Map<MessageType, Consumer<NetworkMessage>> pushListeners = new ConcurrentHashMap<>();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread listenerThread;
    private volatile boolean connected = false;

    private NetworkClient() {
    }

    public static synchronized NetworkClient getInstance() {
        if (instance == null) instance = new NetworkClient();
        return instance;
    }

    public synchronized void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        connected = true;

        listenerThread = new Thread(this::listenLoop, "network-client-listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void listenLoop() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                if (line.isBlank()) continue;

                NetworkMessage message;
                try {
                    message = mapper.readValue(line, NetworkMessage.class);
                } catch (Exception e) {
                    System.err.println("Malformed message from server: " + e.getMessage());
                    continue;
                }

                CompletableFuture<NetworkMessage> waiting = pending.remove(message.getRequestId());
                if (waiting != null) {
                    waiting.complete(message);
                    continue;
                }

                Consumer<NetworkMessage> pushHandler = pushListeners.get(message.getType());
                if (pushHandler != null) {
                    pushHandler.accept(message);
                } else {
                    System.out.println("Unhandled server push: " + message.getType());
                }
            }
        } catch (IOException e) {
            if (connected) System.out.println("Disconnected from server: " + e.getMessage());
        } finally {
            connected = false;
        }
    }

    public NetworkMessage sendAndWait(NetworkMessage request, long timeoutSeconds)
        throws IOException, TimeoutException {
        if (!connected) throw new IOException("Not connected to server");

        CompletableFuture<NetworkMessage> future = new CompletableFuture<>();
        pending.put(request.getRequestId(), future);
        send(request);

        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            pending.remove(request.getRequestId());
            throw e;
        } catch (Exception e) {
            pending.remove(request.getRequestId());
            throw new IOException("Failed while waiting for response", e);
        }
    }

    public synchronized void send(NetworkMessage message) throws IOException {
        if (!connected || out == null) throw new IOException("Not connected to server");
        try {
            out.println(mapper.writeValueAsString(message));
        } catch (Exception e) {
            throw new IOException("Failed to send message", e);
        }
    }

    public void onPush(MessageType type, Consumer<NetworkMessage> handler) {
        pushListeners.put(type, handler);
    }

    public boolean isConnected() {
        return connected;
    }

    public synchronized void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
        }
    }
}
