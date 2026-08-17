package io.java.pvz.net.server;

import io.java.pvz.net.protocol.MessageType;
import io.java.pvz.net.server.game.MatchGameEngine;
import io.java.pvz.net.server.handlers.AuthHandler;
import io.java.pvz.net.server.handlers.LeaderboardHandler;
import io.java.pvz.net.server.handlers.MatchmakingHandler;
import io.java.pvz.net.server.handlers.MatchSyncHandler;
import io.java.pvz.net.server.handlers.ReactionHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {

    private final int port;
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final RequestDispatcher dispatcher = new RequestDispatcher();
    private final SessionRegistry sessionRegistry = new SessionRegistry();
    private final MatchRegistry matchRegistry = new MatchRegistry();
    private final MatchGameEngine matchGameEngine = new MatchGameEngine(matchRegistry);
    private final List<ClientConnection> connections = new CopyOnWriteArrayList<>();

    private MatchmakingHandler matchmakingHandler;

    private ServerSocket serverSocket;
    private volatile boolean running = false;

    public GameServer(int port) {
        this.port = port;
        registerDefaultHandlers();
    }

    private void registerDefaultHandlers() {
        AuthHandler authHandler = new AuthHandler();
        dispatcher.register(MessageType.LOGIN, authHandler::login);
        dispatcher.register(MessageType.LOGOUT, authHandler::logout);
        dispatcher.register(MessageType.SIGNUP_REGISTER, authHandler::signupRegister);
        dispatcher.register(MessageType.SIGNUP_PICK_QUESTION, authHandler::signupPickQuestion);
        dispatcher.register(MessageType.FORGOT_PASSWORD, authHandler::forgotPassword);
        dispatcher.register(MessageType.CHECK_SECURITY_QUESTION, authHandler::checkSecurityQuestion);
        dispatcher.register(MessageType.RESET_PASSWORD, authHandler::resetPassword);
        dispatcher.register(MessageType.FETCH_USER_STATE, authHandler::fetchUserState);

        matchmakingHandler = new MatchmakingHandler(sessionRegistry, matchRegistry, matchGameEngine);
        dispatcher.register(MessageType.CHECK_USER_ONLINE, matchmakingHandler::checkUserOnline);
        dispatcher.register(MessageType.CHALLENGE_INVITE, matchmakingHandler::challengeInvite);
        dispatcher.register(MessageType.CHALLENGE_RESPONSE, matchmakingHandler::challengeResponse);
        dispatcher.register(MessageType.QUEUE_JOIN_RANDOM, matchmakingHandler::queueJoinRandom);
        dispatcher.register(MessageType.QUEUE_LEAVE, matchmakingHandler::queueLeave);

        matchRegistry.setOnEnd(match -> matchGameEngine.cancelMatch(match.getMatchId()));

        MatchSyncHandler matchSyncHandler = new MatchSyncHandler(matchRegistry, matchGameEngine);
        dispatcher.register(MessageType.MATCH_ACTION, matchSyncHandler::handleAction);

        ReactionHandler reactionHandler = new ReactionHandler(matchRegistry);
        dispatcher.register(MessageType.REACTION_SEND, reactionHandler::handleReactionSend);

        LeaderboardHandler leaderboardHandler = new LeaderboardHandler();
        dispatcher.register(MessageType.LEADERBOARD_REQUEST, leaderboardHandler::leaderboardRequest);
        dispatcher.register(MessageType.SCORE_SUBMIT, leaderboardHandler::scoreSubmit);
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        System.out.println("PvZ game server listening on port " + port);

        while (running) {
            try {
                Socket socket = serverSocket.accept();
                ClientConnection connection = new ClientConnection(socket, this);
                connections.add(connection);
                clientPool.submit(connection);
                System.out.println("Client connected: " + socket.getRemoteSocketAddress());
            } catch (IOException e) {
                if (running) System.err.println("Error accepting connection: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
        for (ClientConnection c : connections) c.close();
        clientPool.shutdownNow();
    }

    void removeConnection(ClientConnection connection) {
        connections.remove(connection);
    }

    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }

    public SessionRegistry getSessionRegistry() {
        return sessionRegistry;
    }

    public MatchRegistry getMatchRegistry() {
        return matchRegistry;
    }

    public MatchmakingHandler getMatchmakingHandler() {
        return matchmakingHandler;
    }

    public MatchGameEngine getMatchGameEngine() {
        return matchGameEngine;
    }

    public List<ClientConnection> getConnections() {
        return connections;
    }
}
