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

        if ((matcher = ShopMenuCommands.SHOP_LIST_PLANTS.getMatcher(input)) != null) {
            System.out.println(controller.showPurchasablePlants());

        } else if ((matcher = ShopMenuCommands.SHOP_LIST.getMatcher(input)) != null) {
            System.out.println(controller.showShopList());

        } else if ((matcher = ShopMenuCommands.SHOP_BUY_PLANT.getMatcher(input)) != null) {
            String plantName = matcher.group("plantName");
            System.out.println(controller.buyPlant(plantName));

        } else if ((matcher = ShopMenuCommands.SHOP_BUY.getMatcher(input)) != null) {
            String itemId = matcher.group("itemId");
            int count = Integer.parseInt(matcher.group("count"));
            String plantType = matcher.group("plantType");
            System.out.println(controller.buyItem(itemId, count, plantType));

        } else if ((matcher = ShopMenuCommands.SHOP_DAILY.getMatcher(input)) != null) {
            System.out.println(controller.showDailyDeal());

        } else {
            invalidCommands();
        }
    }
}
