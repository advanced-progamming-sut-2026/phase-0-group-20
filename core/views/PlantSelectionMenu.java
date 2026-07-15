package views;

import controllers.GameController.PlantSelectionController;
import controllers.NavigationController;
import models.enums.commands.MainCommands;
import models.enums.commands.PlantSelectionCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class PlantSelectionMenu implements AppMenu {
    private final PlantSelectionController controller = new PlantSelectionController();

    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if (PlantSelectionCommands.SHOW_ALL_PLANTS.getMatcher(input) != null) {
            System.out.println(controller.showAllPlants());
        } else if (PlantSelectionCommands.SHOW_AVAILABLE_PLANTS.getMatcher(input) != null) {
            System.out.println(controller.showAllAvailablePlants());
        } else if ((matcher = PlantSelectionCommands.ADD_PLANT.getMatcher(input)) != null) {
            System.out.println(controller.addPlant(matcher.group("type")));
        } else if ((matcher = PlantSelectionCommands.REMOVE_PLANT.getMatcher(input)) != null) {
            System.out.println(controller.removePlant(matcher.group("type")));
        } else if ((matcher = PlantSelectionCommands.BOOST_PLANT.getMatcher(input)) != null) {
            System.out.println(controller.boostPlant(matcher.group("type")));
        } else if (PlantSelectionCommands.START_GAME.getMatcher(input) != null) {
            System.out.println(controller.startGame());
        } else {
            invalidCommands();
        }

    }
}
