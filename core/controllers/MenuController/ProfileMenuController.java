package controllers.MenuController;

import models.App;
import models.Result;
import models.database.DataBaseManager;
import models.users.PasswordUtils;
import models.users.User;
import models.validation.UserValidator;

public class ProfileMenuController {

    public Result changeUsername(String newUsername) {
        User current = App.getActiveUser();
        if (current == null)
            return new Result(false, "no user is currently logged in");

        if (current.getUsername().equals(newUsername)) {
            return new Result(false, "username is already in use");
        }

        Result result;
        if (!(result = UserValidator.validateUsername(newUsername)).isSuccessful())
            return result;

        DataBaseManager.updateUsername(current, newUsername);

        return new Result(true, "username has been changed successfully");
    }

    public Result changePassword(String oldPassword, String newPassword) {
        User current = App.getActiveUser();
        if (current == null)
            return new Result(false, "no user is currently logged in");

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

    public Result changeEmail(String newEmail) {
        User current = App.getActiveUser();

        if (current == null)
            return new Result(false, "no user is currently logged in");

        if (current.getEmail().equals(newEmail))
            return new Result(false, "Your new email is the same as your old email");

        Result result;
        if (!(result = UserValidator.validateEmail(newEmail)).isSuccessful())
            return result;

        DataBaseManager.updateEmail(current, newEmail);

        return new Result(true, "email has been changed successfully");
    }

    public Result changeNickname(String newNickname) {
        User current = App.getActiveUser();

        if (current == null)
            return new Result(false, "no user is currently logged in");

        Result result;
        if (!(result = UserValidator.validateNickname(newNickname)).isSuccessful())
            return result;

        DataBaseManager.updateNickname(current, newNickname);

        return new Result(true, "nickname has been changed successfully");
    }

    public Result showUserInfo() {
        User current = App.getActiveUser();

        if (current == null)
            return new Result(false, "no user is currently logged in");

        String userInfo = """
                            ╔══════════════ User Information ══════════════╗
                            👤 Username         : %s
                            📛 Name             : %s
                            🎮 Games Played     : %d
                            🪙 Coins            : %d
                            💎 Diamonds         : %d
                            🏆 Levels Completed : %d
                            ╚══════════════════════════════════════════════╝
                """.formatted(
                current.getUsername(),
                current.getNickname(),
                current.getGamesPlayed(),
                current.getCoin(),
                current.getDiamond(),
                current.getLevelsCompleted()
                // one more property to add later
        );

        return new Result(true, userInfo);
    }
}
