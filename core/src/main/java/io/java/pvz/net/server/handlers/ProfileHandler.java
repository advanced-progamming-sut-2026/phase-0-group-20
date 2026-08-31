package io.java.pvz.net.server.handlers;

import io.java.pvz.controllers.MenuController.ProfileMenuController;
import io.java.pvz.models.Result;
import io.java.pvz.models.users.User;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.ClientConnection;

public class ProfileHandler {

    private final ProfileMenuController controller = new ProfileMenuController();

    public NetworkMessage updateUsername(NetworkMessage request, ClientConnection connection) {
        User current = connection.getAuthenticatedUser();
        String newUsername = request.getString("newUsername");

        Result result = controller.changeUsername(current, newUsername);
        if (!result.isSuccessful()) {
            return NetworkMessage.failure(request, result.message());
        }

        NetworkMessage response = NetworkMessage.success(request);
        response.put("username", current.getUsername());
        return response;
    }

    public NetworkMessage updateEmail(NetworkMessage request, ClientConnection connection) {
        User current = connection.getAuthenticatedUser();
        String newEmail = request.getString("newEmail");

        Result result = controller.changeEmail(current, newEmail);
        if (!result.isSuccessful()) {
            return NetworkMessage.failure(request, result.message());
        }

        NetworkMessage response = NetworkMessage.success(request);
        response.put("email", current.getEmail());
        return response;
    }

    public NetworkMessage updateNickname(NetworkMessage request, ClientConnection connection) {
        User current = connection.getAuthenticatedUser();
        String newNickname = request.getString("newNickname");

        Result result = controller.changeNickname(current, newNickname);
        if (!result.isSuccessful()) {
            return NetworkMessage.failure(request, result.message());
        }

        NetworkMessage response = NetworkMessage.success(request);
        response.put("nickname", current.getNickname());
        return response;
    }

    public NetworkMessage updatePassword(NetworkMessage request, ClientConnection connection) {
        User current = connection.getAuthenticatedUser();
        String oldPassword = request.getString("oldPassword");
        String newPassword = request.getString("newPassword");
        String repeatPassword = request.getString("repeatPassword");

        Result result = controller.changePassword(current, oldPassword, newPassword, repeatPassword);
        if (!result.isSuccessful()) {
            return NetworkMessage.failure(request, result.message());
        }

        return NetworkMessage.success(request);
    }
}
