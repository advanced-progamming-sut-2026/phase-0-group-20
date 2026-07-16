package views;

import controllers.MenuController.ProfileMenuController;
import models.enums.commands.ProfileCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class ProfileMenu implements AppMenu {

    private final ProfileMenuController controller = new ProfileMenuController();

    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = ProfileCommands.CHANGE_USERNAME.getMatcher(input)) != null) {
            System.out.println(controller.changeUsername(matcher.group("username")));
        } else if ((matcher = ProfileCommands.CHANGE_NICKNAME.getMatcher(input)) != null) {
            System.out.println(controller.changeNickname(matcher.group("nickname")));
        } else if ((matcher = ProfileCommands.CHANGE_EMAIL.getMatcher(input)) != null) {
            System.out.println(controller.changeEmail(matcher.group("email")));
        } else if ((matcher = ProfileCommands.CHANGE_PASSWORD.getMatcher(input)) != null) {
            System.out.println(controller.changePassword(matcher.group("old"), matcher.group("new")));
        } else if ((matcher = ProfileCommands.SHOW_INFO.getMatcher(input)) != null) {
            System.out.println(controller.showUserInfo());
        } else {
            invalidCommands();
        }
    }
}
