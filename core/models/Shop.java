package models;

import models.entities.plants.Plant;
import models.users.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Shop {
    public enum CurrencyType {COIN, DIAMOND}

    public static class PlantPrice {
        public int amount;
        public CurrencyType currency;

        public PlantPrice(int amount, CurrencyType currency) {
            this.amount = amount;
            this.currency = currency;
        }
    }

    private static final Map<String, PlantPrice> PREMIUM_PLANTS = new HashMap<>();

    static {
        PREMIUM_PLANTS.put("Garlic", new PlantPrice(4000, CurrencyType.COIN));
        PREMIUM_PLANTS.put("Kernel-pult", new PlantPrice(5000, CurrencyType.COIN));
        PREMIUM_PLANTS.put("Phat Beet", new PlantPrice(5000, CurrencyType.COIN));
        PREMIUM_PLANTS.put("Pea Pod", new PlantPrice(6000, CurrencyType.COIN));
        PREMIUM_PLANTS.put("Primal Potato Mine", new PlantPrice(7000, CurrencyType.COIN));
        PREMIUM_PLANTS.put("Primal Sunflower", new PlantPrice(8000, CurrencyType.COIN));
        PREMIUM_PLANTS.put("Pumpkin", new PlantPrice(8000, CurrencyType.COIN));
        PREMIUM_PLANTS.put("Starfruit", new PlantPrice(9000, CurrencyType.COIN));
        PREMIUM_PLANTS.put("Citron", new PlantPrice(10000, CurrencyType.COIN));
        PREMIUM_PLANTS.put("Winter Melon", new PlantPrice(15000, CurrencyType.COIN));

        PREMIUM_PLANTS.put("Kiwibeast", new PlantPrice(80, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Wasabi Whip", new PlantPrice(80, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Goo Peashooter", new PlantPrice(80, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Cactus", new PlantPrice(80, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Gold Bloom", new PlantPrice(100, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Electric Blueberry", new PlantPrice(100, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Explode-o-nut", new PlantPrice(100, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Ice-shroom", new PlantPrice(100, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Cat-tail", new PlantPrice(120, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Grapeshot", new PlantPrice(120, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Mega Gatling Pea", new PlantPrice(150, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Caulipower", new PlantPrice(150, CurrencyType.DIAMOND));
        PREMIUM_PLANTS.put("Imitater", new PlantPrice(150, CurrencyType.DIAMOND));


        String[] mints = {"Enlighten-mint", "Appease-mint", "Arma-mint", "Bombard-mint",
                "Enforce-mint", "Reinforce-mint", "Enchant-mint", "Pierce-mint", "catTail-mint"};
        for (String mint : mints) {
            PREMIUM_PLANTS.put(mint, new PlantPrice(100, CurrencyType.DIAMOND));
        }
    }

    public String getPurchasablePlantsCatalog(User user) {
        StringBuilder sb = new StringBuilder("=== Premium Plants Shop ===\n");
        boolean hasAvailable = false;

        for (Map.Entry<String, PlantPrice> entry : PREMIUM_PLANTS.entrySet()) {
            String plantName = entry.getKey();
            PlantPrice price = entry.getValue();

            if (!userHasPlant(user, plantName)) {
                sb.append(String.format("- %-20s : %d %s\n", plantName, price.amount, price.currency));
                hasAvailable = true;
            }
        }

        if (!hasAvailable) {
            return "You have unlocked all available premium plants! Awesome!";
        }
        return sb.toString();
    }

    public Result buyPremiumPlant(User user, String plantName) {
        String matchedKey = null;
        PlantPrice price = null;

        for (Map.Entry<String, PlantPrice> entry : PREMIUM_PLANTS.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(plantName)) {
                matchedKey = entry.getKey();
                price = entry.getValue();
                break;
            }
        }

        if (matchedKey == null) {
            return new Result(false, "Plant '" + plantName + "' is not available in the shop!");
        }

        if (userHasPlant(user, matchedKey)) {
            return new Result(false, "You already own " + matchedKey + "!");
        }

        if (price.currency == CurrencyType.COIN) {
            if (user.getCoin() < price.amount) {
                return new Result(false, "Not enough coins! (Needs " + price.amount + ")");
            }
            user.costCoin(price.amount);
        } else {
            if (user.getDiamond() < price.amount) {
                return new Result(false, "Not enough diamonds! (Needs " + price.amount + ")");
            }
            user.costDiamond(price.amount);
        }

        Plant newPlant = App.findPlantByName(matchedKey);
        if (newPlant != null) {
            user.getUnlockedPlants().add(newPlant);
            return new Result(true, "Congratulations! You successfully bought " + matchedKey +
                    " for " + price.amount + " " + price.currency + "!");
        }

        return new Result(false, "System error: Plant object not found.");
    }

    private boolean userHasPlant(User user, String plantName) {
        for (Plant p : user.getUnlockedPlants()) {
            if (p.getName().equalsIgnoreCase(plantName)) {
                return true;
            }
        }
        return false;
    }


    public String getCatalog() {
        return "=== Crazy Dave's Shop ===\n" +
                "1. Pot (Max 20) - 2000 Coins\n" +
                "2. Plant Food (Max 3) - 3 Diamonds\n" +
                "3. Random Seed Packet (5x Pack) - 1000 Coins\n" +
                "4. Selective Seed Packet (10x Pack) - 5 Diamonds\n" +
                "5. Currency Exchange (500 Coins) - 5 Diamonds";
    }

    public String getDailyDeal(User user) {
        if (user.isPurchasedDailyDealToday()) {
            return "Daily Deal: Sold out for today!";
        }
        Plant dailyPlant = getDailyRandomPlant(user);
        if (dailyPlant == null) return "Daily Deal: Not available yet.";

        return "Daily Deal: 10x " + dailyPlant.getName() + " Seed Packets - 1600 Coins (20% Off!)";
    }

    private Plant getDailyRandomPlant(User user) {
        if (user.getUnlockedPlants() == null || user.getUnlockedPlants().isEmpty()) return null;
        long todaySeed = LocalDate.now().toEpochDay();
        Random dailyRandom = new Random(todaySeed);
        return user.getUnlockedPlants().get(dailyRandom.nextInt(user.getUnlockedPlants().size()));
    }

    public Result buyPot(User user, int count) {
        int totalCost = 2000 * count;
        if (user.getCoin() < totalCost) return new Result(false, "Not enough coins! (Needs " + totalCost + ")");

        int unlockedCount = 0;
        var pots = user.getGreenHouse().getPots();
        for (int i = 0; i < pots.length; i++) {
            for (int j = 0; j < pots[i].length; j++) {
                if (pots[i][j].getPotCondition() != models.greenhouse.PotCondition.LOCKED) {
                    unlockedCount++;
                }
            }
        }

        if (unlockedCount + count > 20) {
            return new Result(false, "Cannot buy " + count + " pots. Greenhouse limit is 20!");
        }

        int unlockedThisPurchase = 0;
        for (int i = 0; i < pots.length && unlockedThisPurchase < count; i++) {
            for (int j = 0; j < pots[i].length && unlockedThisPurchase < count; j++) {
                if (pots[i][j].getPotCondition() == models.greenhouse.PotCondition.LOCKED) {
                    pots[i][j].setPotCondition(models.greenhouse.PotCondition.EMPTY);
                    unlockedThisPurchase++;
                }
            }
        }

        user.costCoin(totalCost);
        return new Result(true, "Successfully purchased " + count + " Pot(s)!");
    }

    public Result buyPlantFood(User user, int count) {
        int totalCost = 3 * count;
        if (user.getDiamond() < totalCost) return new Result(false, "Not enough diamonds! (Needs " + totalCost + ")");

        if (user.getPlantFoodCount() + count > 3) {
            return new Result(false, "Cannot hold more than 3 Plant Foods in your inventory!");
        }

        user.costDiamond(totalCost);
        user.setPlantFoodCount(user.getPlantFoodCount() + count);
        return new Result(true, "Successfully bought " + count + " Plant Food(s)!");
    }

    public Result buyRandomSeedPacket(User user, int count) {
        int totalCost = 1000 * count;
        if (user.getCoin() < totalCost) return new Result(false, "Not enough coins! (Needs " + totalCost + ")");
        if (user.getUnlockedPlants().isEmpty()) return new Result(false, "You must unlock a plant first!");

        user.costCoin(totalCost);
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            Plant randomPlant = user.getUnlockedPlants().get(rand.nextInt(user.getUnlockedPlants().size()));
            user.getInventory().addSeedPacket(randomPlant, 5);
        }

        return new Result(true, "Successfully purchased " + count + " Random Seed Packet(s)!");
    }

    public Result buySelectiveSeedPacket(User user, int count, String plantName) {
        int totalCost = 5 * count;
        if (user.getDiamond() < totalCost) return new Result(false, "Not enough diamonds! (Needs " + totalCost + ")");

        Plant selectedPlant = null;
        for (Plant p : user.getUnlockedPlants()) {
            if (p.getName().equalsIgnoreCase(plantName)) {
                selectedPlant = p;
                break;
            }
        }

        if (selectedPlant == null) return new Result(false, "You haven't unlocked a plant named " + plantName + "!");

        user.costDiamond(totalCost);
        user.getInventory().addSeedPacket(selectedPlant, 10 * count);
        return new Result(true, "Successfully purchased "
                + count + " Selective Seed Packet(s) for " + selectedPlant.getName() + "!");
    }

    public Result exchangeCurrency(User user, int count) {
        int totalCost = 5 * count;
        if (user.getDiamond() < totalCost) return new Result(false, "Not enough diamonds! (Needs " + totalCost + ")");

        user.costDiamond(totalCost);
        user.earnCoin(500 * count);
        return new Result(true, "Successfully exchanged " + totalCost + " Diamonds for " + (500 * count) + " Coins!");
    }

    public Result buyDailyItem(User user) {
        if (user.isPurchasedDailyDealToday())
            return new Result(false, "You have already purchased today's daily deal!");
        if (user.getCoin() < 1600)
            return new Result(false, "Not enough coins! (Needs 1600)");

        Plant dailyPlant = getDailyRandomPlant(user);
        if (dailyPlant == null) return new Result(false, "No daily deal available.");

        user.costCoin(1600);
        user.setPurchasedDailyDealToday(true);
        user.getInventory().addSeedPacket(dailyPlant, 10);
        return new Result(true, "Successfully purchased the Daily Deal! 10x " + dailyPlant.getName() + " seeds.");
    }
}