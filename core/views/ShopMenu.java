package views;

import controllers.GameController.ShopMenuController;
import models.enums.commands.ShopMenuCommands;

import java.util.Scanner;
import java.util.regex.Matcher;

public class ShopMenu implements AppMenu {
    ShopMenuController controller = new ShopMenuController();


    public void check(Scanner scanner) {
        String input = scanner.nextLine().trim();

        if (checkGlobalCommand(input)) {
            return;
        }

        Matcher matcher;

        if ((matcher = ShopMenuCommands.SHOW_SHOP.getMatcher(input)) != null) {
            System.out.println(controller.showShop());
        } else if ((matcher = ShopMenuCommands.BUY_ITEM.getMatcher(input)) != null) {
            System.out.println(controller.buyItem(matcher.group("item")));
        } else {
            invalidCommands();
        }
    }
}
