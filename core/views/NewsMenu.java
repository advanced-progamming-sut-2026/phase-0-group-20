package views;

import controllers.GameController.NewsController;
import controllers.NavigationController;
import models.enums.commands.MainCommands;
import models.enums.commands.NewsCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class NewsMenu implements AppMenu {
    private final NewsController controller =  new NewsController();
    @Override
    public void check(Scanner scanner) {
        String input = scanner.nextLine();
        Matcher matcher;

        if((matcher = NewsCommands.SHOW_UNREAD_NEWS.getMatcher(input))!=null){
            System.out.println(controller.showUnreadNews());
        }

        else if ((matcher = NewsCommands.SHOW_ALL_NEWS.getMatcher(input))!=null){
            System.out.println(controller.showAllNews());
        }
        else if ((matcher = MainCommands.EXIT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.exitMenu());
        } else if ((matcher = MainCommands.ENTER_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.enterMenu(matcher.group("name")));
        } else if ((matcher = MainCommands.SHOW_CURRENT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.showCurrentMenu());
        }
    }
}
