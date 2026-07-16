package views;

import controllers.MenuController.LoginMenuController;
import models.enums.commands.LoginCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class LoginMenu implements AppMenu {

    private final LoginMenuController controller = new LoginMenuController();

    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = LoginCommands.LOGIN.getMatcher(input)) != null) {
            boolean stayLoggedIn = matcher.group("stayLoggedIn") != null;
            System.out.println(controller.login(
                    matcher.group("username"),
                    matcher.group("password"),
                    stayLoggedIn
            ));
        } else if ((matcher = LoginCommands.FORGOT_PASSWORD.getMatcher(input)) != null) {
            System.out.println(controller.forgetPassword(
                    matcher.group("username"),
                    matcher.group("email")));
        } else if ((matcher = LoginCommands.ANSWER_QUESTION.getMatcher(input)) != null) {
            System.out.println(controller.checkSecurityQuestion(matcher.group("answer")));
        } else if ((matcher = LoginCommands.RESET_PASSWORD.getMatcher(input)) != null) {
            System.out.println(controller.resetPassword(
                    matcher.group("password"),
                    matcher.group("confirm")
            ));
        } else {
            invalidCommands();
        }
    }
}