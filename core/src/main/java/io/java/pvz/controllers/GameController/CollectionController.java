package io.java.pvz.controllers.GameController;

import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.users.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CollectionController {
    private static final int MAX_LEVEL = 4;
    private static final int BASE_COST = 200;
    private static final int BASE_SEED_PACKETS = 10;
    private static final int PURCHASE_COST = 2000;

    public Result showPlants() {
        User activeUser = App.getActiveUser();
        ArrayList<Plant> unlockedPlants = activeUser.getUnlockedPlants();
        StringBuilder result = new StringBuilder();
        if (unlockedPlants.isEmpty()) {
            result.append("You have no plants in this game!");
            return new Result(false, result.toString());
        }
        for (Plant plant : unlockedPlants) {
            result.append(getPlantInfo(plant));
        }
        result.deleteCharAt(result.length() - 1);
        return new Result(true, result.toString());
    }

    public Result showAllPlants() {

        ArrayList<Plant> plants = App.getAllPlants();
        StringBuilder result = new StringBuilder();
        for (Plant plant : plants) {
            result.append(getPlantInfo(plant));
        }
        result.deleteCharAt(result.length() - 1);
        return new Result(true, result.toString());
    }

    public Result showZombies() {
        User activeUser = App.getActiveUser();
        ArrayList<Zombie> zombies = activeUser.getUnlockedZombies();
        StringBuilder result = new StringBuilder();
        for (Zombie zombie : zombies) {
            result.append(getZombieInfo(zombie));
        }
        result.deleteCharAt(result.length() - 1);
        return new Result(true, result.toString());
    }

    public Result showAllZombies() {
        ArrayList<Zombie> zombies = App.getAllZombies();
        StringBuilder result = new StringBuilder();
        for (Zombie zombie : zombies) {
            result.append(getZombieInfo(zombie));
        }
        result.deleteCharAt(result.length() - 1);
        return new Result(true, result.toString());

    }

    public Result showPlantInfo(String name) {
        ArrayList<Plant> plants = App.getAllPlants();
        Plant foundPlant = plants.stream()
                .filter(plant -> plant.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        if (foundPlant == null) {
            return new Result(false, "Your desired plant doesn't exist.");
        }
        String rawText = getPlantInfo(foundPlant);
        String text = rawText.substring(0, rawText.length() - 1);
        return new Result(true, text);
    }

    public Result showZombieInfo(String name) {
        ArrayList<Zombie> zombies = App.getAllZombies();
        Zombie foundZombie = zombies.stream()
                .filter(zombie -> zombie.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        if (foundZombie == null) {
            return new Result(false, "Your desired zombie doesn't exist.");
        }
        String rawText = getZombieInfo(foundZombie);
        String text = rawText.substring(0, rawText.length() - 1);
        return new Result(true, text);
    }

    public Result upgradePlant(String name) {
        User activeUser = App.getActiveUser();
        String capitalName = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        ArrayList<Plant> plants = activeUser.getUnlockedPlants();
        Plant foundPlant = plants.stream()
                .filter(plant -> plant.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        boolean exist = App.getAllPlants().stream()
                .anyMatch(plant -> plant.getName().equalsIgnoreCase(name));
        if (exist && foundPlant == null) {
            return new Result(false, "You haven't unlocked this plant yet");
        } else if (foundPlant == null) {
            return new Result(false, "Your desired plant doesn't exist.");
        }
        HashMap<String, Integer> seeds = activeUser.getInventory().getSeedPackets();
        int cost = BASE_COST * foundPlant.getLevel();
        int mathPower = (int) Math.pow(2, foundPlant.getLevel());
        int seedPacketCost = BASE_SEED_PACKETS * mathPower;
        if (!seeds.containsKey(capitalName) || seedPacketCost > seeds.get(capitalName)) {
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                    .message("Not Enough Seed Packets")
                    .build());
            return new Result(false, "You don't have enough seed packets to upgrade this plant.");
        }
        if (cost > activeUser.getCoin()) {
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                    .message("Not Enough Coin. COST: " + cost)
                    .build());
            return new Result(false, "You don't have enough coin to upgrade this plant.");
        }
        if (foundPlant.getLevel() == MAX_LEVEL) {
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                    .message("The plant is already at max level")
                    .build());
            return new Result(false, "The plant is already at max level.");
        }
        foundPlant.upgrade();
        activeUser.costCoin(cost);
        seeds.computeIfPresent(capitalName, (k, v) -> Math.max(0, v - seedPacketCost));
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY)
                .message("Successfully upgraded to LVL " + foundPlant.getLevel())
                .build());
        return new Result(true, "You successfully upgraded " + foundPlant.getName() +
                " to level " + foundPlant.getLevel() + ".");
    }

    public Result purchasePlant(String name) {
        User activeUser = App.getActiveUser();
        ArrayList<Plant> userPlants = activeUser.getUnlockedPlants();
        ArrayList<Plant> plants = App.getAllPlants();
        Plant foundPlant = plants.stream()
                .filter(plant -> plant.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
        if (foundPlant == null) {
            return new Result(false, "Your desired plant doesn't exist.");
        }
        for (Plant p : userPlants) {
            if (p.getName().equalsIgnoreCase(name)) {
                return new Result(false, "You already have this plant.");
            }
        }
        if (activeUser.getCoin() < BASE_COST) {
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                    .message("Not Enough Coin. COST: " + PURCHASE_COST)
                    .build());
            return new Result(false, "You don't have enough coin to purchase this plant.");
        }
        activeUser.costCoin(PURCHASE_COST);
        userPlants.add(foundPlant);
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY)
                .message("Successfully purchased "+foundPlant.getName())
                .build());
        return new Result(true, "You successfully purchased " + foundPlant.getName() + " .");

    }

    private String formatTags(List<PlantTag> tags) {
        if (tags.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (PlantTag tag : tags) {
            result.append(tag.name().toLowerCase()).append(", ");
        }
        result.delete(result.length() - 2, result.length());
        return result.toString();
    }

    private String getPlantInfo(Plant plant) {
        StringBuilder plantInfo = new StringBuilder();
        plantInfo.append("-------------------------------\n");
        String format = "%-15s : %s%n";
        plantInfo.append(String.format(format, "Name", plant.getName()));
        String formatedTags = formatTags(plant.getTags());
        plantInfo.append(String.format(format, "Tags", formatedTags));
        plantInfo.append(String.format(format, "Category", plant.getCategory().name().toLowerCase())); // not permanent
        plantInfo.append(String.format(format, "Damage", plant.getDamage()));
        plantInfo.append(String.format(format, "Base HP", plant.getBaseHp()));
        plantInfo.append(String.format(format, "Base Ability", plant.getAbilityType()));
        plantInfo.append("-------------------------------\n\n");
        return plantInfo.toString();
    }

    private String getZombieInfo(Zombie zombie) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("-------------------------------\n");
        String format = "%-15s : %s%n";
        stringBuilder.append(String.format(format, "Name", zombie.getName()));
        stringBuilder.append(String.format(format, "Health", zombie.getBaseHp()));
        stringBuilder.append(String.format(format, "Speed", zombie.getBaseSpeed()));
        stringBuilder.append("-------------------------------\n\n");
        // not full
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    public int getBaseCost() {
        return BASE_COST;
    }
}


