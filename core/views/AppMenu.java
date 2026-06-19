package views;

import java.util.Scanner;

public interface AppMenu {
    void check(Scanner scanner);

    default void invalidCommands() {
        System.out.println("Invalid command");
    }
}
