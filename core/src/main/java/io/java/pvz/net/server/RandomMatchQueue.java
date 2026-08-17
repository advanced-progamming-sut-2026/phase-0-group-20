package io.java.pvz.net.server;


public class RandomMatchQueue {

    private final Object lock = new Object();
    private ClientConnection waiting;

    public MatchSession join(ClientConnection connection) {
        synchronized (lock) {
            if (waiting != null && waiting != connection) {
                ClientConnection opponent = waiting;
                waiting = null;
                return MatchSession.ofRandomRoles(opponent, connection);
            }
            waiting = connection;
            return null;
        }
    }

    public void leave(ClientConnection connection) {
        synchronized (lock) {
            if (waiting == connection) waiting = null;
        }
    }

    public boolean isWaiting(ClientConnection connection) {
        synchronized (lock) {
            return waiting == connection;
        }
    }
}
