package views;

import java.util.Scanner;
import java.util.regex.Matcher;

public class LeaderBoard implements AppMenu {
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();
        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;
    }
}
