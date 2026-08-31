package io.java.pvz.net.server.handlers;

import io.java.pvz.models.Result;
import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.users.PasswordUtils;
import io.java.pvz.models.users.User;
import io.java.pvz.models.validation.UserValidator;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.ClientConnection;

public class ProfileHandler {

    public NetworkMessage updateUsername(NetworkMessage request, ClientConnection connection) {
        User current = connection.getAuthenticatedUser();
        if (current == null) {
            return NetworkMessage.failure(request, "not logged in");
        }

        String newUsername = request.getString("newUsername");
        if (newUsername != null) newUsername = newUsername.trim();

        if (newUsername == null || newUsername.equals(current.getUsername())) {
            return NetworkMessage.failure(request, "username is already in use");
        }

        Result validation = UserValidator.validateUsername(newUsername);
        if (!validation.isSuccessful()) {
            return NetworkMessage.failure(request, validation.message());
        }

        if (DataBaseManager.usernameExists(newUsername)) {
            return NetworkMessage.failure(request, "username is already taken");
        }

        DataBaseManager.updateUsername(current, newUsername);

        NetworkMessage response = NetworkMessage.success(request);
        response.put("username", current.getUsername());
        return response;
    }

    public NetworkMessage updateEmail(NetworkMessage request, ClientConnection connection) {
        User current = connection.getAuthenticatedUser();
        if (current == null) {
            return NetworkMessage.failure(request, "not logged in");
        }

        String newEmail = request.getString("newEmail");
        if (newEmail != null) newEmail = newEmail.trim();

        if (newEmail == null || newEmail.equals(current.getEmail())) {
            return NetworkMessage.failure(request, "your new email is the same as your old email");
        }

        Result validation = UserValidator.validateEmail(newEmail);
        if (!validation.isSuccessful()) {
            return NetworkMessage.failure(request, validation.message());
        }

        DataBaseManager.updateEmail(current, newEmail);

        NetworkMessage response = NetworkMessage.success(request);
        response.put("email", current.getEmail());
        return response;
    }

    public NetworkMessage updateNickname(NetworkMessage request, ClientConnection connection) {
        User current = connection.getAuthenticatedUser();
        if (current == null) {
            return NetworkMessage.failure(request, "not logged in");
        }

        String newNickname = request.getString("newNickname");
        if (newNickname != null) newNickname = newNickname.trim();

        Result validation = UserValidator.validateNickname(newNickname);
        if (!validation.isSuccessful()) {
            return NetworkMessage.failure(request, validation.message());
        }

        DataBaseManager.updateNickname(current, newNickname);

        NetworkMessage response = NetworkMessage.success(request);
        response.put("nickname", current.getNickname());
        return response;
    }

    public NetworkMessage updatePassword(NetworkMessage request, ClientConnection connection) {
        User current = connection.getAuthenticatedUser();
        if (current == null) {
            return NetworkMessage.failure(request, "not logged in");
        }

        String oldPassword = request.getString("oldPassword");
        String newPassword = request.getString("newPassword");
        String repeatPassword = request.getString("repeatPassword");

        Result matchResult = UserValidator.validatePasswordsMatch(newPassword, repeatPassword);
        if (!matchResult.isSuccessful()) {
            return NetworkMessage.failure(request, matchResult.message());
        }

        String hashOldPassword = PasswordUtils.hashPassword(oldPassword);
        if (!current.getPasswordHash().equals(hashOldPassword)) {
            return NetworkMessage.failure(request, "password does not match!");
        }

        Result validation = UserValidator.validatePassword(newPassword);
        if (!validation.isSuccessful()) {
            return NetworkMessage.failure(request, validation.message());
        }

        String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
        if (current.getPasswordHash().equals(hashedNewPassword)) {
            return NetworkMessage.failure(request, "your new password is the same as your old password");
        }

        boolean updated = DataBaseManager.updatePassword(current, oldPassword, newPassword);
        if (!updated) {
            return NetworkMessage.failure(request, "password does not match!");
        }

        return NetworkMessage.success(request);
    }
}
