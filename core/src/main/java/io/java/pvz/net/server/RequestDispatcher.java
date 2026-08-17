package io.java.pvz.net.server;

import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.protocol.NetworkMessage;

import java.util.EnumMap;
import java.util.Map;

public class RequestDispatcher {

    private final Map<MessageType, RequestHandler> handlers = new EnumMap<>(MessageType.class);

    public void register(MessageType type, RequestHandler handler) {
        handlers.put(type, handler);
    }

    public NetworkMessage dispatch(NetworkMessage request, ClientConnection connection) {
        if (request.getType() == null) {
            return NetworkMessage.failure(request, "Message is missing a type");
        }

        RequestHandler handler = handlers.get(request.getType());
        if (handler == null) {
            return NetworkMessage.failure(request, "Unsupported message type: " + request.getType());
        }

        try {
            return handler.handle(request, connection);
        } catch (Exception e) {
            System.err.println("Handler for " + request.getType() + " threw an exception: " + e);
            e.printStackTrace();
            return NetworkMessage.failure(request, "Internal server error: " + e.getMessage());
        }
    }
}
