package io.java.pvz.controllers.GameController;

import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.users.User;

import java.util.ArrayList;
import java.util.HashMap;

public class CollectionController {
    private static final int MAX_LEVEL = 4;
    private static final int BASE_COST = 200;
    private static final int BASE_SEED_PACKETS = 10;
    private static final int PURCHASE_COST = 2000;

    public Result upgradePlant(String name) {
        User activeUser = App.getActiveUser();
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
        if (!seeds.containsKey(foundPlant.getName()) || seedPacketCost > seeds.get(foundPlant.getName())) {
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
        seeds.computeIfPresent(foundPlant.getName(), (k, v) -> Math.max(0, v - seedPacketCost));
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
        if (activeUser.getCoin() < PURCHASE_COST) {
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

    public int getMaxLevel() {
        return MAX_LEVEL;
    }

}


