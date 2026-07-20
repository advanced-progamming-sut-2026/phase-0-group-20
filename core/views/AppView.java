package views;

import models.App;
import models.database.DataBaseManager;
import models.enums.Menu;
import models.game.adventure.Adventure;
import models.users.User;

import java.util.Scanner;

public class AppView {
    private static final Scanner sc = new Scanner(System.in);


    public static void run() {
        ConsoleNotifier.register();

        User stayedUser = DataBaseManager.getLoggedInUser();



        if (stayedUser != null) {
            App.setActiveUser(stayedUser);
            App.setActiveMenu(Menu.MAIN_MENU);
            System.out.println("Welcome back, " + stayedUser.getUsername() + "!");

        }

        while (true) {
            App.getActiveMenu().checkCommand(sc);
        }
    }
}
