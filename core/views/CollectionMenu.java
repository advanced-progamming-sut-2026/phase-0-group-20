package views;

import controllers.GameController.CollectionController;
import controllers.NavigationController;
import models.enums.commands.CollectionCommands;
import models.enums.commands.MainCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class CollectionMenu implements AppMenu {
    private final CollectionController controller = new CollectionController();

    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = CollectionCommands.SHOW_PLANTS.getMatcher(input)) != null) {
            System.out.println(controller.showPlants());
        } else if ((matcher = CollectionCommands.SHOW_ALL_PLANTS.getMatcher(input)) != null) {
            System.out.println(controller.showAllPlants());
        } else if ((matcher = CollectionCommands.SHOW_ZOMBIES.getMatcher(input)) != null) {
            System.out.println(controller.showZombies());
        } else if ((matcher = CollectionCommands.SHOW_ALL_ZOMBIES.getMatcher(input)) != null) {
            System.out.println(controller.showAllZombies());
        } else if ((matcher = CollectionCommands.SHOW_PLANT.getMatcher(input)) != null) {
            String plantName = matcher.group("plantName");
            System.out.println(controller.showPlantInfo(plantName));
        } else if ((matcher = CollectionCommands.SHOW_ZOMBIE.getMatcher(input)) != null) {
            String zombieName = matcher.group("zombieName");
            System.out.println(controller.showZombieInfo(zombieName));
        } else if ((matcher = CollectionCommands.UPGRADE_PLANT.getMatcher(input)) != null) {
            String plantName = matcher.group("plantName");
            System.out.println(controller.upgradePlant(plantName));
        } else if ((matcher = CollectionCommands.PURCHASE_PLANT.getMatcher(input)) != null) {
            String plantName = matcher.group("plantName");
            System.out.println(controller.purchasePlant(plantName));
        } else invalidCommands();


    }
}
