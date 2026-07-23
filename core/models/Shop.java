package models;

import models.entities.plants.Plant;
import models.users.User;

import java.time.LocalDate;
import java.util.Random;

public class Shop {

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
        return new Result(true, "Successfully purchased " + count + " Selective Seed Packet(s) for " + selectedPlant.getName() + "!");
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
        if (user.getCoin() < 1600) return new Result(false, "Not enough coins! (Needs 1600)");

        Plant dailyPlant = getDailyRandomPlant(user);
        if (dailyPlant == null) return new Result(false, "No daily deal available.");

        user.costCoin(1600);
        user.setPurchasedDailyDealToday(true);
        user.getInventory().addSeedPacket(dailyPlant, 10);
        return new Result(true, "Successfully purchased the Daily Deal! 10x " + dailyPlant.getName() + " seeds.");
    }
}