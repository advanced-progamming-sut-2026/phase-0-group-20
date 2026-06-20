package controllers.MenuController;

import models.App;
import models.Result;
import models.database.DataBaseManager;
import models.enums.LoginMenuStatus;
import models.enums.Menu;
import models.users.PasswordUtils;
import models.users.User;
import models.validation.UserValidator;

public class LoginMenuController {
    private User pendingResetUser;
    private boolean securityAnswerVerified;
    private LoginMenuStatus loginMenuStatus = LoginMenuStatus.NORMAL;

    public Result login(String username, String password, boolean stayLoggedIn) {
        loginMenuStatus = LoginMenuStatus.NORMAL;
        username = username.trim();
        clearPendingReset();

        if (!DataBaseManager.usernameExists(username))
            return new Result(false, "username does not exist");

        User user = DataBaseManager.authenticateUser(username, password);
        if (user == null)
            return new Result(false, "incorrect password");

        user.setStayLoggedIn(stayLoggedIn);
        DataBaseManager.saveOrUpdateUser(user);
        App.activeUser = user;
        App.activeMenu = Menu.MAIN_MENU;

        return new Result(true, "welcome " + user.getNickname());
    }


    public Result forgetPassword(String username, String email) {
        clearPendingReset();
        if (loginMenuStatus != LoginMenuStatus.NORMAL)
            return new Result(false, "invalid command");
        User user = DataBaseManager.getUserForRecovery(username.trim(), email);
        if (user == null)
            return new Result(false, "user doesn't exist");

        pendingResetUser = user;
        loginMenuStatus = LoginMenuStatus.FORGET_COMMAND;
        return new Result(true, user.getSecurityQuestion().toString());
    }


    public Result checkSecurityQuestion(String answer) {
        if (loginMenuStatus != LoginMenuStatus.FORGET_COMMAND)
            return new Result(false, "invalid command");
        if (pendingResetUser == null)
            return new Result(false, "you must request a password reset first");

        if (!PasswordUtils.hashPassword(answer).equals(pendingResetUser.getSecurityAnswerHash())) {
            clearPendingReset();
            loginMenuStatus = LoginMenuStatus.NORMAL;
            return new Result(false, "incorrect answer, please start over");
        }

        securityAnswerVerified = true;
        loginMenuStatus = LoginMenuStatus.ANSWER_QUESTION;
        return new Result(true, "correct answer, please enter your new password and repeat it");
    }


    public Result resetPassword(String newPassword, String confirmPassword) {
        if (loginMenuStatus != LoginMenuStatus.ANSWER_QUESTION)
            return new Result(false, "invalid command");
        if (pendingResetUser == null || !securityAnswerVerified)
            return new Result(false, "you must answer the security question first");

        Result result;
        if (!(result = UserValidator.validatePassword(newPassword)).isSuccessful()) return result;
        if (!(result = UserValidator.validatePasswordsMatch(newPassword, confirmPassword)).isSuccessful())
            return result;

        pendingResetUser.setPasswordHash(PasswordUtils.hashPassword(newPassword));
        DataBaseManager.saveOrUpdateUser(pendingResetUser);
        clearPendingReset();
        loginMenuStatus = LoginMenuStatus.RESET_PASSWORD;
        return new Result(true, "password changed successfully, you can now login");
    }


    private void clearPendingReset() {
        pendingResetUser = null;
        securityAnswerVerified = false;
    }

}
