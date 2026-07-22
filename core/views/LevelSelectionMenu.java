package views;

import controllers.GameController.GameMenuController;
import models.enums.commands.GameMenuCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class LevelSelectionMenu implements AppMenu {
    private final GameMenuController controller = new GameMenuController();

    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();
        Matcher matcher;
        if ((matcher = GameMenuCommands.ENTER_LEVEL.getMatcher(input)) != null) {
            String levelStr = matcher.group("levelNumber");
            System.out.println(controller.enterLevel(levelStr));
        } else if (checkGlobalCommand(input))
            return;
        else
            invalidCommands();

    }
}