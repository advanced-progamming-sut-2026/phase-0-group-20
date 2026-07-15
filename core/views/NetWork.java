package views;

import java.util.Scanner;
import java.util.regex.Matcher;

public class NetWork implements AppMenu {
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;
    }
}
