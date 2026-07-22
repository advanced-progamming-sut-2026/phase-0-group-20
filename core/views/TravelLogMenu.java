package views;

import controllers.GameController.MiniGameController;
import controllers.GameController.TravelLogController;
import models.enums.commands.TravelLogCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class TravelLogMenu implements AppMenu {
    private final TravelLogController controller = new TravelLogController();
    private final MiniGameController miniGameController = new MiniGameController();

    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = TravelLogCommands.CHANGE_PAGE.getMatcher(input)) != null) {
            String pageName = matcher.group("pageName");
            System.out.println(controller.changePage(pageName));
        } else if ((matcher = TravelLogCommands.ENTER_MINIGAME.getMatcher(input)) != null) {
            String minigameName = matcher.group("name");
            String levelName = matcher.group("level");
            System.out.println(controller.startMiniGame(minigameName, levelName));
        } else if ((matcher = TravelLogCommands.SHOW_PAGE.getMatcher(input)) != null) {
            System.out.println(controller.showCurrentPage());
        } else {
            invalidCommands();
        }
    }
}