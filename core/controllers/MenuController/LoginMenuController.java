package controllers.MenuController;

import models.App;
import models.Result;
import models.database.DataBaseManager;
import models.enums.LoginMenuStatus;
import models.enums.Menu;
import models.game.adventure.Adventure;
import models.users.PasswordUtils;
import models.users.User;
import models.validation.UserValidator;

public class LoginMenuController {
    private User pendingResetUser;
    private boolean securityAnswerVerified;
    private LoginMenuStatus lastLoginMenuStatus = LoginMenuStatus.MAIN_FLOW;

    public Result login(String username, String password, boolean stayLoggedIn) {
        lastLoginMenuStatus = LoginMenuStatus.MAIN_FLOW;
        username = username.trim();
        clearPendingReset();

        if (!DataBaseManager.usernameExists(username))
            return new Result(false, "username does not exist");

        User user = DataBaseManager.authenticateUser(username, password);
        if (user == null)
            return new Result(false, "incorrect password");

        user.setStayLoggedIn(stayLoggedIn);
        DataBaseManager.saveOrUpdateUser(user);
        App.setActiveUser(user);
        App.setActiveAdventure(new Adventure());
        App.setActiveMenu(Menu.MAIN_MENU);

        return new Result(true, "welcome " + user.getNickname());
    }


    public Result forgetPassword(String username, String email) {
        clearPendingReset();
        if (lastLoginMenuStatus != LoginMenuStatus.MAIN_FLOW)
            return new Result(false, "invalid command");
        User user = DataBaseManager.getUserForRecovery(username.trim(), email);
        if (user == null)
            return new Result(false, "user doesn't exist");

        pendingResetUser = user;
        lastLoginMenuStatus = LoginMenuStatus.FORGET_PASSWORD;
        String question = user.getSecurityQuestion().getQuestion();
        return new Result(true, question);
    }


    public Result checkSecurityQuestion(String answer) {
        if (lastLoginMenuStatus != LoginMenuStatus.FORGET_PASSWORD)
            return new Result(false, "invalid command");
        if (pendingResetUser == null)
            return new Result(false, "you must request a password reset first");

        if (!PasswordUtils.hashPassword(answer).equals(pendingResetUser.getSecurityAnswerHash())) {
            clearPendingReset();
            lastLoginMenuStatus = LoginMenuStatus.MAIN_FLOW;
            return new Result(false, "incorrect answer, please start over");
        }

        securityAnswerVerified = true;
        lastLoginMenuStatus = LoginMenuStatus.ANSWER_QUESTION;
        return new Result(true, "correct answer, please enter your new password and repeat it");
    }


    public Result resetPassword(String newPassword, String confirmPassword) {
        if (lastLoginMenuStatus != LoginMenuStatus.ANSWER_QUESTION)
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
        lastLoginMenuStatus = LoginMenuStatus.RESET_PASSWORD;
        return new Result(true, "password changed successfully, you can now login");
    }


    private void clearPendingReset() {
        pendingResetUser = null;
        securityAnswerVerified = false;
    }

}
