package views;

import controllers.GameController.NewsController;
import models.enums.commands.NewsCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class NewsMenu implements AppMenu {
    private final NewsController controller = new NewsController();

    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = NewsCommands.SHOW_UNREAD_NEWS.getMatcher(input)) != null) {
            System.out.println(controller.showUnreadNews());
        } else if ((matcher = NewsCommands.SHOW_ALL_NEWS.getMatcher(input)) != null) {
            System.out.println(controller.showAllNews());
        } else {
            invalidCommands();
        }
    }
}
