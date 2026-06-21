package views;

import models.App;
import models.database.DataBaseManager;
import models.enums.Menu;
import models.users.User;

import java.util.Scanner;

public class AppView {
    private static final Scanner sc = new Scanner(System.in);


    public static void run() {
        User stayedUser = DataBaseManager.getLoggedInUser();

        if (stayedUser != null) {
            App.activeUser = stayedUser;
            App.activeMenu = Menu.MAIN_MENU;
            System.out.println("Welcome back, " + stayedUser.getUsername() + "!");
        }

        while (true) {
            App.activeMenu.checkCommand(sc);
        }
    }
}
