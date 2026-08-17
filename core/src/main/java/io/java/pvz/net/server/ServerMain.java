package io.java.pvz.net.server;

import io.java.pvz.models.database.DataBaseManager;

public class ServerMain {

    public static final int DEFAULT_PORT = 5454;

    public static void main(String[] args) throws Exception {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port '" + args[0] + "', using default " + DEFAULT_PORT);
            }
        }

        DataBaseManager.initializeDatabase();

        GameServer server = new GameServer(port);

        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
    }
}
