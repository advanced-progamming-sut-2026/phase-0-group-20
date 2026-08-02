package com.Project.PVZ.views;

import com.Project.PVZ.models.App;
import com.Project.PVZ.models.database.DataBaseManager;
import com.Project.PVZ.models.enums.Menu;
import com.Project.PVZ.models.game.adventure.Adventure;
import com.Project.PVZ.models.users.User;

import java.util.Scanner;

public class AppView {

    public static void run() {
        Scanner sc = new Scanner(System.in);
        ConsoleNotifier.register();

        User stayedUser = DataBaseManager.getLoggedInUser();
        if (stayedUser == null) {
            App.setActiveAdventure(new Adventure());
        }

        if (stayedUser != null) {
            App.setActiveUser(stayedUser);
            App.setActiveMenu(Menu.MAIN_MENU);
            App.setActiveAdventure(new Adventure());
            System.out.println("Welcome back, " + stayedUser.getUsername() + "!");
            App.setAllUsers(DataBaseManager.getAllUsers());
        }

        while (true) {
            App.getActiveMenu().checkCommand(sc);
        }
    }
}
