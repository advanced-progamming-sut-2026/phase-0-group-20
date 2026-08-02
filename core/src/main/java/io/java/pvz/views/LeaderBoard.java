package io.java.pvz.views;

import io.java.pvz.controllers.GameController.LeaderBoardController;
import io.java.pvz.models.enums.commands.LeaderBoardCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class LeaderBoard implements AppMenu {
    private final LeaderBoardController controller = new LeaderBoardController();


    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = LeaderBoardCommands.SHOW.getMatcher(input)) != null) {
            System.out.println(controller.showResults());
        } else if ((matcher = LeaderBoardCommands.SORT.getMatcher(input)) != null) {
            String type = matcher.group("type");
            System.out.println(controller.changeSortType(type));
        } else {
            invalidCommands();
        }
    }
}
