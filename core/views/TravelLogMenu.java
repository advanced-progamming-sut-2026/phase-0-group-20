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
        String input = scanner.nextLine();
        Matcher matcher;

        if ((matcher = MainCommands.EXIT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.exitMenu());
        } else if ((matcher = MainCommands.ENTER_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.enterMenu(matcher.group("name")));
        } else if ((matcher = MainCommands.SHOW_CURRENT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.showCurrentMenu());

        } else if ((matcher = TravelLogCommands.SHOW_PAGE.getMatcher(input)) != null) {
            String pageName = matcher.group("pageName");
            System.out.println(controller.showTravelLogPage(pageName));
        } else if ((matcher = TravelLogCommands.NEXT_PAGE.getMatcher(input)) != null) {
            System.out.println(controller.nextPage());

        } else {
            invalidCommands();
        }
    }
}