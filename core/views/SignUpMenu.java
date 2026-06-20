package views;

import controllers.MenuController.SignupMenuController;
import controllers.NavigationController;
import models.enums.commands.MainCommands;
import models.enums.commands.SignUpCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class SignUpMenu implements AppMenu {

    private final SignupMenuController controller = new SignupMenuController();

    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();
        Matcher matcher;

        if ((matcher = MainCommands.EXIT_MENU.getMatcher(input)) != null) {
            System.exit(0);
        } else if ((matcher = MainCommands.ENTER_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.enterMenu(matcher.group("name")));
        } else if ((matcher = MainCommands.SHOW_CURRENT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.showCurrentMenu());
        } else if ((matcher = SignUpCommands.REGISTER.getMatcher(input)) != null) {
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