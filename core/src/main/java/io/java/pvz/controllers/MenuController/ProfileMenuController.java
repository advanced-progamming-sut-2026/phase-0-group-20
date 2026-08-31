package io.java.pvz.controllers.MenuController;

import io.java.pvz.models.Result;
import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.users.PasswordUtils;
import io.java.pvz.models.users.User;
import io.java.pvz.models.validation.UserValidator;

public class ProfileMenuController {

    public Result changeUsername(User current, String newUsername) {
        if (current == null)
            return new Result(false, "no user is currently logged in");

        if (newUsername != null) newUsername = newUsername.trim();

        if (current.getUsername().equals(newUsername)) {
            return new Result(false, "username is already in use");
        }

        Result result;
        if (!(result = UserValidator.validateUsername(newUsername)).isSuccessful())
            return result;

        if (DataBaseManager.usernameExists(newUsername)) {
            return new Result(false, "username is already taken");
        }

        DataBaseManager.updateUsername(current, newUsername);
        return new Result(true, "username has been changed successfully");
    }

    public Result changePassword(User current, String oldPassword, String newPassword, String repeatPassword) {
        if (current == null)
            return new Result(false, "No user is currently logged in");

        Result matchResult = UserValidator.validatePasswordsMatch(newPassword, repeatPassword);
        if (!matchResult.isSuccessful())
            return matchResult;

        String hashOldPassword = PasswordUtils.hashPassword(oldPassword);
        if (!current.getPasswordHash().equals(hashOldPassword))
            return new Result(false, "password does not match!");

        Result result;
        if (!(result = UserValidator.validatePassword(newPassword)).isSuccessful())
            return result;

        String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
        if (current.getPasswordHash().equals(hashedNewPassword))
            return new Result(false, "Your new password is the same as your old password");

        DataBaseManager.updatePassword(current, oldPassword, newPassword);
        return new Result(true, "password has been changed successfully");
    }

    public Result changeEmail(User current, String newEmail) {
        if (current == null)
            return new Result(false, "no user is currently logged in");

        if (newEmail != null) newEmail = newEmail.trim();

        if (current.getEmail().equals(newEmail))
            return new Result(false, "Your new email is the same as your old email");

        Result result;
        if (!(result = UserValidator.validateEmail(newEmail)).isSuccessful())
            return result;

        DataBaseManager.updateEmail(current, newEmail);
        return new Result(true, "email has been changed successfully");
    }

    public Result changeNickname(User current, String newNickname) {
        if (current == null)
            return new Result(false, "no user is currently logged in");

        if (newNickname != null) newNickname = newNickname.trim();

        Result result;
        if (!(result = UserValidator.validateNickname(newNickname)).isSuccessful())
            return result;

        DataBaseManager.updateNickname(current, newNickname);
        return new Result(true, "nickname has been changed successfully");
    }
}
