package views;

import controllers.GameController.GreenHouseController;
import controllers.NavigationController;
import models.enums.commands.GreenHouseCommands;
import models.enums.commands.MainCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class GreenHouseMenu implements AppMenu {
    private final GreenHouseController controller = new GreenHouseController();

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
        } else if ((matcher = GreenHouseCommands.SHOW_GREENHOUSE.getMatcher(input)) != null) {
            System.out.println(controller.showGreenHouse());
        } else if ((matcher = GreenHouseCommands.PLANT_POT.getMatcher(input)) != null) {
            String x = matcher.group("x");
            String y = matcher.group("y");
            controller.plantPot(x,y);
        } else if ((matcher = GreenHouseCommands.COLLECT_POT.getMatcher(input)) != null) {
            String x = matcher.group("x");
            String y = matcher.group("y");
            controller.collect(x,y);
        } else if ((matcher = GreenHouseCommands.GROW_POT.getMatcher(input)) != null) {
            String x = matcher.group("x");
            String y = matcher.group("y");
            controller.grow(x,y);
        } else {
            invalidCommands();
        }
    }
}