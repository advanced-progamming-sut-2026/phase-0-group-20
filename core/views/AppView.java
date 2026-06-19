package views;

import models.App;

import java.util.Scanner;

public class AppView {
    private static final Scanner sc = new Scanner(System.in);
    public static void run() {
        while (true) {
            App.activeMenu.checkCommand(sc);
        }
    }
}
