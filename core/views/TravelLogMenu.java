package views;

import controllers.GameController.TravelLogController;
import controllers.NavigationController;
import models.enums.commands.MainCommands;
import models.enums.commands.TravelLogCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class TravelLogMenu implements AppMenu {
    private final TravelLogController controller = new TravelLogController();

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
        } else if ((matcher = TravelLogCommands.SHOW_PAGE.getMatcher(input)) != null) {
            System.out.println(controller.showCurrentPage());
        } else {
            invalidCommands();
        }
    }
}