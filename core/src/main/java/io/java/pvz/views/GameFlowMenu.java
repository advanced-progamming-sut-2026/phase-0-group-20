package io.java.pvz.views;

import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.controllers.GameController.GameMapController;
import io.java.pvz.controllers.GameController.MiniGameController;
import io.java.pvz.models.enums.commands.GameFlowCommands;
import io.java.pvz.models.enums.commands.MiniGameCommands;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.minigame.IMinigame;

import java.util.Scanner;
import java.util.regex.Matcher;

public class GameFlowMenu implements AppMenu {
    private final GameFlowController gameFlowcontroller = new GameFlowController();
    private final MiniGameController miniGameController = new MiniGameController();
    private final GameMapController gameMapController = new GameMapController();

    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

    }
}
