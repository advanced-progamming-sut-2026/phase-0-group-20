package views;

import controllers.MenuController.MainMenuController;
import controllers.NavigationController;
import models.enums.commands.MainCommands;
import models.enums.commands.MainMenuCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class MainMenu implements AppMenu {

    private final MainMenuController controller = new MainMenuController();


    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();
        Matcher matcher;

        if ((matcher = MainMenuCommands.LOGOUT.getMatcher(input)) != null) {
            System.out.println(controller.logout());
        } else if ((matcher = MainCommands.ENTER_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.enterMenu(matcher.group("menu")));
        } else if ((matcher = MainCommands.SHOW_CURRENT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.showCurrentMenu());
        } else if ((matcher = MainCommands.EXIT_MENU.getMatcher(input)) != null) {
            System.out.println("if you want to exit you must logout!");
        } else {
            invalidCommands();
        }
    }
}
