package controllers.GameController;

import models.App;
import models.Result;
import models.Shop;
import models.users.User;

public class ShopMenuController {
    private final Shop shop = new Shop();

    public Result showShop() {
        return new Result(true, shop.getCatalog());
    }

    public Result buyItem(String itemName) {
        User activeUser = App.getActiveUser();
        if (activeUser == null) {
            return new Result(false, "No active user found!");
        }

        String item = itemName.toLowerCase().trim();

        return switch (item) {
            case "pot" -> shop.buyPot(activeUser);
            case "plant food", "plantfood" -> shop.buyPlantFood(activeUser);
            case "seed packet", "random seed packet" -> shop.buyRandomSeedPacket(activeUser);
            case "daily deal", "dailyitem" -> shop.buyDailyItem(activeUser);
            default -> new Result(false, "Item not found in shop!");
        };
    }
}
