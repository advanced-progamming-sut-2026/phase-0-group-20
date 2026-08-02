package com.Project.PVZ.views;

import com.Project.PVZ.controllers.GameController.GreenHouseController;
import com.Project.PVZ.models.App;
import com.Project.PVZ.models.enums.commands.GreenHouseCommands;
import com.Project.PVZ.models.greenhouse.GreenHouse;

import java.util.Scanner;
import java.util.regex.Matcher;

public class GreenHouseMenu implements AppMenu {
    private final GreenHouseController controller = new GreenHouseController();

    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        GreenHouse greenHouse = null;
        if (App.getActiveUser() != null) {
            greenHouse = App.getActiveUser().getGreenHouse();
        }

        if ((matcher = GreenHouseCommands.SHOW_GREENHOUSE.getMatcher(input)) != null) {
            System.out.println(controller.showGreenHouse(greenHouse));
        } else if ((matcher = GreenHouseCommands.PLANT_POT.getMatcher(input)) != null) {
            String x = matcher.group("x");
            String y = matcher.group("y");
            System.out.println(controller.plantPot(x, y, greenHouse));
        } else if ((matcher = GreenHouseCommands.COLLECT_POT.getMatcher(input)) != null) {
            String x = matcher.group("x");
            String y = matcher.group("y");
            System.out.println(controller.collect(x, y, greenHouse));
        } else if ((matcher = GreenHouseCommands.GROW_POT.getMatcher(input)) != null) {
            String x = matcher.group("x");
            String y = matcher.group("y");
            System.out.println(controller.grow(x, y, greenHouse));
        } else {
            invalidCommands();
        }
    }
}

