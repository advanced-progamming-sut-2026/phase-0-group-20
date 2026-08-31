package io.java.pvz.net.server.handlers;

import io.java.pvz.controllers.MenuController.LoginMenuController;
import io.java.pvz.controllers.MenuController.SignupMenuController;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.users.PasswordUtils;
import io.java.pvz.models.users.User;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.ClientConnection;
import io.java.pvz.net.server.SessionRegistry;

public class AuthHandler {

    private final SessionRegistry sessionRegistry;

    public AuthHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public NetworkMessage login(NetworkMessage request, ClientConnection connection) {
        LoginMenuController controller = connection.getLoginController();

        String username = request.getString("username");
        String password = request.getString("password");
        boolean stayLoggedIn = Boolean.TRUE.equals(request.getBoolean("stayLoggedIn"));

        if (username == null || password == null) {
            return NetworkMessage.failure(request, "username and password are required");
        }

        if (sessionRegistry.isOnline(username)) {
            return NetworkMessage.failure(request, "this account is already logged in from another session");
        }

        Result result = controller.login(username, password, stayLoggedIn);

        if (!result.isSuccessful()) {
            return NetworkMessage.failure(request, result.message());
        }

        User user = App.getActiveUser();
        connection.setAuthenticatedUser(user);

        NetworkMessage response = NetworkMessage.success(request);
        response.put("user", user);

        response.put("nickname", user.getNickname());
        response.put("coin", user.getCoin());
        response.put("diamond", user.getDiamond());
        response.put("plantFoodCount", user.getPlantFoodCount());
        return response;
    }

    public NetworkMessage logout(NetworkMessage request, ClientConnection connection) {
        User user = connection.getAuthenticatedUser();
        if (user != null) {
            DataBaseManager.logoutUser(user.getId());
        }
        connection.setAuthenticatedUser(null);
        return NetworkMessage.success(request);
    }

    public NetworkMessage signupRegister(NetworkMessage request, ClientConnection connection) {
        SignupMenuController controller = connection.getSignupController();

        Result result = controller.register(
            request.getString("username"),
            request.getString("password"),
            request.getString("repeatPassword"),
            request.getString("nickname"),
            request.getString("email"),
            request.getString("gender")
        );

        return toNetworkMessage(request, result, "securityQuestionsText");
    }

    public NetworkMessage signupPickQuestion(NetworkMessage request, ClientConnection connection) {
        SignupMenuController controller = connection.getSignupController();

        Result result = controller.pickQuestion(
            request.getString("questionNumber"),
            request.getString("answer"),
            request.getString("confirmAnswer")
        );

        return toNetworkMessage(request, result, "message");
    }

    public NetworkMessage forgotPassword(NetworkMessage request, ClientConnection connection) {
        String username = request.getString("username");
        String email = request.getString("email");

        User user = DataBaseManager.getUserForRecovery(username, email);
        if (user == null) {
            return NetworkMessage.failure(request, "username and email do not match");
        }

        NetworkMessage response = NetworkMessage.success(request);
        response.put("securityQuestion", user.getSecurityQuestion().getQuestion());
        return response;
    }

    public NetworkMessage checkSecurityQuestion(NetworkMessage request, ClientConnection connection) {
        String username = request.getString("username");
        String answer = request.getString("answer");

        User user = DataBaseManager.getAllUsers().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst().orElse(null);

        if (user == null) {
            return NetworkMessage.failure(request, "user not found");
        }

        String hashedAnswer = PasswordUtils.hashPassword(answer);
        if (!user.getSecurityAnswerHash().equals(hashedAnswer)) {
            return NetworkMessage.failure(request, "wrong answer to security question");
        }

        return NetworkMessage.success(request);
    }

    public NetworkMessage resetPassword(NetworkMessage request, ClientConnection connection) {
        String username = request.getString("username");
        String newPassword = request.getString("newPassword");

        String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
        DataBaseManager.updateForgotPassword(username, hashedNewPassword);

        return NetworkMessage.success(request);
    }

    public NetworkMessage fetchUserState(NetworkMessage request, ClientConnection connection) {
        User user = connection.getAuthenticatedUser();
        if (user == null) {
            return NetworkMessage.failure(request, "not logged in");
        }

        NetworkMessage response = NetworkMessage.success(request);
        response.put("coin", user.getCoin());
        response.put("diamond", user.getDiamond());
        response.put("plantFoodCount", user.getPlantFoodCount());
        response.put("gamesPlayed", user.getGamesPlayed());
        response.put("levelsCompleted", user.getLevelsCompleted());
        response.put("highestBonusScore", user.getHighestBonusScore());
        return response;
    }

    private NetworkMessage toNetworkMessage(NetworkMessage request, Result result, String messageKey) {
        if (!result.isSuccessful()) {
            return NetworkMessage.failure(request, result.message());
        }
        NetworkMessage response = NetworkMessage.success(request);
        response.put(messageKey, result.message());
        return response;
    }
}
