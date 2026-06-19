package views;

import models.enums.commands.LoginCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class LoginMenu implements AppMenu {
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();
        Matcher matcher;

        if ((matcher = LoginCommands.LOGIN.getMatcher(input)).matches()) {

        } else if ((matcher = LoginCommands.ANSWER_QUESTION.getMatcher(input)).matches()) {

        } else if ((matcher = LoginCommands.FORGOT_PASSWORD.getMatcher(input)).matches()) {

        } else {
            invalidCommands();
        }
    }
}
