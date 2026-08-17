package io.java.pvz.net.server;

import io.java.pvz.net.protocol.NetworkMessage;

@FunctionalInterface
public interface RequestHandler {
    NetworkMessage handle(NetworkMessage request, ClientConnection connection);
}
