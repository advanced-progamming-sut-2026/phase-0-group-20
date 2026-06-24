package views;

import controllers.GameController.SettingController;
import controllers.NavigationController;
import models.enums.commands.MainCommands;
import models.enums.commands.SettingCommands;
//CHECK
import java.util.Scanner;
import java.util.regex.Matcher;

public class Setting implements AppMenu {
    private final SettingController settingController = new SettingController();

    public void check(Scanner scanner) {
        String input = scanner.nextLine();
        Matcher matcher;
        if ((matcher = SettingCommands.CHANGE_DIFFICULTY.getMatcher(input)) != null) {
            System.out.println(settingController.changeDifficulty(matcher.group("level")));
        } else if ((matcher = MainCommands.EXIT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.exitMenu());
        } else if ((matcher = MainCommands.ENTER_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.enterMenu(matcher.group("name")));
        } else if ((matcher = MainCommands.SHOW_CURRENT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.showCurrentMenu());
        }
    }
}
