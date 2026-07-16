package views;

import controllers.GameController.SettingController;
import models.enums.commands.SettingCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class Setting implements AppMenu {
    private final SettingController settingController = new SettingController();

    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = SettingCommands.CHANGE_DIFFICULTY.getMatcher(input)) != null) {
            System.out.println(settingController.changeDifficulty(matcher.group("level")));
        } else {
            invalidCommands();
        }
    }
}
