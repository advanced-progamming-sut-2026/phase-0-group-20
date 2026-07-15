package views;

import controllers.GameController.GameFlowController;
import controllers.NavigationController;
import models.enums.commands.GameFlowCommands;
import models.enums.commands.MainCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class GameFlowMenu implements AppMenu {
    private final GameFlowController controller = new GameFlowController();

    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = GameFlowCommands.ADVANCE_TIME.getMatcher(input)) != null) {
            System.out.println(controller.advanceTime(matcher.group("count")));

        } else if ((matcher = GameFlowCommands.COLLECT_SUN.getMatcher(input)) != null) {
            System.out.println(controller.collectSun(
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else if ((matcher = GameFlowCommands.SHOW_SUN_AMOUNT.getMatcher(input)) != null) {
            System.out.println(controller.showSunAmount());

        } else if ((matcher = GameFlowCommands.CHEAT_ADD_SUN.getMatcher(input)) != null) {
            System.out.println(controller.cheatAddSun(matcher.group("count")));

        } else if ((matcher = GameFlowCommands.RELEASE_NUKE.getMatcher(input)) != null) {
            System.out.println(controller.releaseNuke());

        } else if ((matcher = GameFlowCommands.PLANT_PLANT.getMatcher(input)) != null) {
            System.out.println(controller.plantPlant(
                    matcher.group("plantName"),
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else if ((matcher = GameFlowCommands.CHEAT_REMOVE_COOLDOWN.getMatcher(input)) != null) {
            System.out.println(controller.cheatRemoveCooldown());

        } else if ((matcher = GameFlowCommands.PLUCK_PLANT.getMatcher(input)) != null) {
            System.out.println(controller.pluckPlant(
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else if ((matcher = GameFlowCommands.FEED_PLANT.getMatcher(input)) != null) {
            System.out.println(controller.feedPlant(
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else if ((matcher = GameFlowCommands.CHEAT_ADD_PLANT_FOOD.getMatcher(input)) != null) {
            System.out.println(controller.cheatAddPlantFood());

        } else if ((matcher = GameFlowCommands.SHOW_MAP.getMatcher(input)) != null) {
            System.out.println(controller.showMap());

        } else if ((matcher = GameFlowCommands.SHOW_PLANTS_STATUS.getMatcher(input)) != null) {
            System.out.println(controller.showPlantsStatus());

        } else if ((matcher = GameFlowCommands.SHOW_TILE_STATUS.getMatcher(input)) != null) {
            System.out.println(controller.showTileStatus(
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else {
            invalidCommands();
        }
    }

}
