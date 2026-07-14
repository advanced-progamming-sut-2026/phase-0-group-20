package models;

import models.entities.plants.Plant;
import models.users.User;

import java.util.Random;

public class Shop {
    private int potLimit = 5;
    private int plantFoodLimit = 3;

    public String getCatalog() {
        return "=== Crazy Dave's Shop ===\n" +
                "1. Pot (For Greenhouse) - 500 Coins\n" +
                "2. Plant Food - 1000 Coins\n" +
                "3. Random Seed Packet - 2000 Coins\n" +
                "4. Daily Deal (Random Plant Boost) - 15 Diamonds";
    }

    public Result buyPot(User user) { // for green house
        if (user.getCoin() < 500) {
            return new Result(false, "Not enough coins! (Needs 500)");
        }

        boolean unlocked = false;
        var pots = user.getGreenHouse().getPots();
        for (int i = 0; i < pots.length; i++) {
            for (int j = 0; j < pots[i].length; j++) {
                if (pots[i][j].getPotCondition() == models.greenhouse.PotCondition.LOCKED) {
                    pots[i][j].setPotCondition(models.greenhouse.PotCondition.EMPTY);
                    unlocked = true;
                    break;
                }
            }
            if (unlocked) break;
        }

        if (!unlocked) {
            return new Result(false, "All your pots are already unlocked!");
        }

        user.earnCoin(-500);
        return new Result(true, "Successfully purchased a Pot!");
    }

    public Result buyPlantFood(User user) {
        if (user.getCoin() < 1000) {
            return new Result(false, "Not enough coins! (Needs 1000)");
        }

        user.earnCoin(-1000);
        if (!user.getUnlockedPlants().isEmpty()) {
            Plant p = user.getUnlockedPlants().get(0); // it would be different
            user.getInventory().addFoodPlant(p);
            return new Result(true, "Successfully bought Plant Food for " + p.getName() + "!");
        }

        return new Result(true, "Successfully bought Plant Food!");
    }

    public Result buyRandomSeedPacket(User user) {
        if (user.getCoin() < 2000) {
            return new Result(false, "Not enough coins! (Needs 2000)");
        }

        if (App.getAllPlants() == null || App.getAllPlants().isEmpty()) {
            return new Result(false, "No plants available in the game catalog!");
        }

        Random rand = new Random();
        Plant randomPlant = App.getAllPlants().get(rand.nextInt(App.getAllPlants().size()));

        user.earnCoin(-2000);
        user.getInventory().addSeedPacket(randomPlant);

        return new Result(true, "Successfully purchased a Seed Packet for: " + randomPlant.getName());
    }

    public Result buyDailyItem(User user) {
        if (user.getDiamond() < 15) {
            return new Result(false, "Not enough diamonds! (Needs 15)");
        }

        user.earnDiamond(-15);
        return new Result(true, "Successfully purchased the Daily Deal! Your next game is boosted.");
    }
}
