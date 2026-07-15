package views;

import controllers.NavigationController;
import models.enums.commands.MainCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public interface AppMenu {
    void check(Scanner scanner);

    default boolean checkGlobalCommand(String input) {
        Matcher matcher;

        if ((matcher = MainCommands.EXIT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.exitMenu());
            return true;
        } else if ((matcher = MainCommands.ENTER_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.enterMenu(matcher.group("name")));
            return true;
        } else if ((matcher = MainCommands.SHOW_CURRENT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.showCurrentMenu());
            return true;
        }

        return false;
    }

    default void invalidCommands() {
        System.out.println("Invalid command");
    }
}
