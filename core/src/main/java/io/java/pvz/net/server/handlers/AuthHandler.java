package io.java.pvz.net.server.handlers;

import io.java.pvz.controllers.MenuController.SignupMenuController;
import io.java.pvz.models.Result;
import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.users.User;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.ClientConnection;

public class AuthHandler {

    public NetworkMessage login(NetworkMessage request, ClientConnection connection) {
        String username = request.getString("username");
        String password = request.getString("password");
        boolean stayLoggedIn = Boolean.TRUE.equals(request.getBoolean("stayLoggedIn"));
        boolean isHash = Boolean.TRUE.equals(request.getBoolean("isHash")); // 🌟 فیکس مهم: چک کردن هش بودن پسورد

        if (username == null || password == null) {
            return NetworkMessage.failure(request, "username and password are required");
        }
        username = username.trim();

        if (!DataBaseManager.usernameExists(username)) {
            return NetworkMessage.failure(request, "username does not exist");
        }

        User user;
        if (isHash) {
            String finalUsername = username;
            user = DataBaseManager.getAllUsers().stream()
                .filter(u -> u.getUsername().equals(finalUsername) && u.getPasswordHash().equals(password))
                .findFirst().orElse(null);
        } else {
            user = DataBaseManager.authenticateUser(username, password);
        }

        if (user == null) {
            return NetworkMessage.failure(request, "incorrect password");
        }

        user.setStayLoggedIn(stayLoggedIn);
        DataBaseManager.saveOrUpdateUser(user);
        user.performDailyLoginCheck();

        connection.setAuthenticatedUser(user);

        NetworkMessage response = NetworkMessage.success(request);
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
        Result result = connection.getLoginController().forgetPassword(
            request.getString("username"),
            request.getString("email")
        );
        return toNetworkMessage(request, result, "securityQuestion");
    }

    public NetworkMessage checkSecurityQuestion(NetworkMessage request, ClientConnection connection) {
        Result result = connection.getLoginController().checkSecurityQuestion(request.getString("answer"));
        return toNetworkMessage(request, result, "message");
    }

    public NetworkMessage resetPassword(NetworkMessage request, ClientConnection connection) {
        Result result = connection.getLoginController().resetPassword(
            request.getString("newPassword"),
            request.getString("confirmPassword")
        );
        return toNetworkMessage(request, result, "message");
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
