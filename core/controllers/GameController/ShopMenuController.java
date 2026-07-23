package controllers.GameController;

import models.App;
import models.Result;
import models.Shop;
import models.users.User;

public class ShopMenuController {
    private final Shop shop = new Shop();

    public Result showShopList() {
        return new Result(true, shop.getCatalog());
    }

    public Result showDailyDeal() {
        User activeUser = App.getActiveUser();
        if (activeUser == null) return new Result(false, "No active user found!");
        return new Result(true, shop.getDailyDeal(activeUser));
    }

    public Result showPurchasablePlants() {
        User activeUser = App.getActiveUser();
        if (activeUser == null) return new Result(false, "No active user found!");
        return new Result(true, shop.getPurchasablePlantsCatalog(activeUser));
    }

    public Result buyPlant(String plantName) {
        User activeUser = App.getActiveUser();
        if (activeUser == null) return new Result(false, "No active user found!");

        return shop.buyPremiumPlant(activeUser, plantName);
    }

    public Result buyItem(String itemName, int count, String targetPlant) {
        User activeUser = App.getActiveUser();
        if (activeUser == null) {
            return new Result(false, "No active user found!");
        }

        String item = itemName.toLowerCase().trim();

        return switch (item) {
            case "pot" -> shop.buyPot(activeUser, count);
            case "plant food", "plantfood" -> shop.buyPlantFood(activeUser, count);
            case "random seed packet" -> shop.buyRandomSeedPacket(activeUser, count);
            case "selective seed packet" -> {
                if (targetPlant == null || targetPlant.isEmpty()) {
                    yield new Result(false, "You must specify a plant type (-t) for selective seed packets!");
                }
                yield shop.buySelectiveSeedPacket(activeUser, count, targetPlant);
            }
            case "currency exchange" -> shop.exchangeCurrency(activeUser, count);
            case "daily deal", "dailyitem" -> {
                if (count > 1) {
                    yield new Result(false, "The Daily Deal can only be purchased once per day!");
                }
                yield shop.buyDailyItem(activeUser);
            }
            default -> new Result(false, "Item not found in shop!");
        };
    }
}