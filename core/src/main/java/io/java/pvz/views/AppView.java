package io.java.pvz.views;

import io.java.pvz.models.App;
import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.adventure.Adventure;
import io.java.pvz.models.users.User;

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
