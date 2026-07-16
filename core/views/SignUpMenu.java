package views;

import controllers.MenuController.SignupMenuController;
import models.enums.commands.SignUpCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class SignUpMenu implements AppMenu {

    private final SignupMenuController controller = new SignupMenuController();

    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = SignUpCommands.REGISTER.getMatcher(input)) != null) {
            System.out.println(controller.register(
                    matcher.group("username"),
                    matcher.group("password"),
                    matcher.group("confirm"),
                    matcher.group("nickname"),
                    matcher.group("email"),
                    matcher.group("gender")
            ));
        } else if ((matcher = SignUpCommands.PICK_QUESTION.getMatcher(input)) != null) {
            System.out.println(controller.pickQuestion(
                    matcher.group("number"),
                    matcher.group("answer"),
                    matcher.group("confirm")
            ));
        } else {
            invalidCommands();
        }
    }
}