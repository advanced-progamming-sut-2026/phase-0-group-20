package views;

import models.enums.commands.SignUpCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class SignUpMenu implements AppMenu {
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();
        Matcher matcher;

        if ((matcher = SignUpCommands.REGISTER.getMatcher(input)).matches()) {

        } else if ((matcher = SignUpCommands.PICK_QUESTION.getMatcher(input)).matches()) {

        } else {
            invalidCommands();
        }
    }
}
