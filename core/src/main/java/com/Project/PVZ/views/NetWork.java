package com.Project.PVZ.views;

import java.util.Scanner;

public class NetWork implements AppMenu {
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

    }
}
