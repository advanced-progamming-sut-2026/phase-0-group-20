package views;

import controllers.GameController.GameFlowController;
import controllers.GameController.MiniGameController;
import models.enums.commands.GameFlowCommands;
import models.enums.commands.MiniGameCommands;
import models.game.GameSession;
import models.game.minigame.IZombieLevel;

import java.util.Scanner;
import java.util.regex.Matcher;

public class GameFlowMenu implements AppMenu {
    private final GameFlowController gameFlowcontroller = new GameFlowController();
    private final MiniGameController miniGameController = new MiniGameController();


    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = GameFlowCommands.ADVANCE_TIME.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.advanceTime(matcher.group("count")));

        } else if ((matcher = GameFlowCommands.COLLECT_SUN.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.collectSun(
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else if (GameFlowCommands.SHOW_STATE.getMatcher(input) != null) {
            System.out.println(gameFlowcontroller.showCurrentState());
        } else if ((matcher = GameFlowCommands.SHOW_SUN_AMOUNT.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.showSunAmount());

        } else if ((matcher = GameFlowCommands.CHEAT_ADD_SUN.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.cheatAddSun(matcher.group("count")));

        } else if ((matcher = GameFlowCommands.RELEASE_NUKE.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.releaseNuke());

        } else if ((matcher = GameFlowCommands.PLANT_PLANT.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.plantPlant(
                    matcher.group("plantName"),
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else if ((matcher = GameFlowCommands.CHEAT_REMOVE_COOLDOWN.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.cheatRemoveCooldown());

        } else if ((matcher = GameFlowCommands.PLUCK_PLANT.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.pluckPlant(
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else if ((matcher = GameFlowCommands.FEED_PLANT.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.feedPlant(
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else if ((matcher = GameFlowCommands.CHEAT_ADD_PLANT_FOOD.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.cheatAddPlantFood());

        } else if ((matcher = GameFlowCommands.SHOW_MAP.getMatcher(input)) != null) {

            GameSession session = GameSession.getInstance();

            if (session.getCurrentMode() instanceof IZombieLevel)
                System.out.println(miniGameController.showMap());
            else
                System.out.println(gameFlowcontroller.showMap());

        } else if ((matcher = GameFlowCommands.SHOW_PLANTS_STATUS.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.showPlantsStatus());

        } else if ((matcher = GameFlowCommands.SHOW_TILE_STATUS.getMatcher(input)) != null) {
            System.out.println(gameFlowcontroller.showTileStatus(
                    matcher.group("x"),
                    matcher.group("y")
            ));

        } else if ((matcher = MiniGameCommands.BREAK_VASE.getMatcher(input)) != null) {
            System.out.println(miniGameController.breakVase(
                    Integer.parseInt(matcher.group("x")),
                    Integer.parseInt(matcher.group("y"))
            ));

        } else if ((matcher = MiniGameCommands.PUT_ZOMBIE.getMatcher(input)) != null) {
            System.out.println(miniGameController.handlePutZombie(
                    matcher.group("zombieName"),
                    Integer.parseInt(matcher.group("x")),
                    Integer.parseInt(matcher.group("y"))
            ));

        } else if ((matcher = MiniGameCommands.BOWL.getMatcher(input)) != null) {
            System.out.println(miniGameController.plantBowlingNut(
                    Integer.parseInt(matcher.group("index")),
                    Integer.parseInt(matcher.group("x")),
                    Integer.parseInt(matcher.group("y"))
            ));

        } else {
            invalidCommands();
        }
    }

}
