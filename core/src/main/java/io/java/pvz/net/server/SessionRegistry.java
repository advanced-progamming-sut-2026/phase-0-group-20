package io.java.pvz.net.server;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionRegistry {

    private final Map<String, ClientConnection> onlineByUsername = new ConcurrentHashMap<>();

    public void register(String username, ClientConnection connection) {
        if (username != null) onlineByUsername.put(username, connection);
    }

    public void unregister(String username) {
        if (username != null) onlineByUsername.remove(username);
    }

    public boolean isOnline(String username) {
        return username != null && onlineByUsername.containsKey(username);
    }

    public ClientConnection get(String username) {
        return username == null ? null : onlineByUsername.get(username);
    }

    public Collection<ClientConnection> allOnline() {
        return onlineByUsername.values();
    }
}
