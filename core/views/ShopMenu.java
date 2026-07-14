package views;

import controllers.GameController.ShopMenuController;
import controllers.NavigationController;
import models.enums.commands.MainCommands;
import models.enums.commands.ShopMenuCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class ShopMenu implements AppMenu {
    ShopMenuController controller = new ShopMenuController();


    public void check(Scanner scanner) {
        String input = scanner.nextLine();

        Matcher matcher;

        if ((matcher = MainCommands.EXIT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.exitMenu());
        } else if ((matcher = MainCommands.ENTER_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.enterMenu(matcher.group("name")));
        } else if ((matcher = MainCommands.SHOW_CURRENT_MENU.getMatcher(input)) != null) {
            System.out.println(NavigationController.showCurrentMenu());
        } else if ((matcher = ShopMenuCommands.SHOW_SHOP.getMatcher(input)) != null) {
            System.out.println(controller.showShop());
        } else if ((matcher = ShopMenuCommands.BUY_ITEM.getMatcher(input)) != null) {
            System.out.println(controller.buyItem(matcher.group("item")));
        } else {
            invalidCommands();
        }
    }
}
