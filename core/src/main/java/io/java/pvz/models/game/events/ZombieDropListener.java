package io.java.pvz.models.game.events;

import io.java.pvz.models.App;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.greenhouse.GreenHouse;
import io.java.pvz.models.greenhouse.Pot;
import io.java.pvz.models.greenhouse.PotCondition;
import io.java.pvz.models.users.User;

import java.util.Random;

public class ZombieDropListener implements GameEventListener {
    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (!(event == GameEvent.ZOMBIE_KILLED_LAWN_MOWER || event == GameEvent.ZOMBIE_KILLED)) return;
        Zombie target = payload.getZombie();
        if (target != null && target.isShiny()) {
            User user = App.getActiveUser();
            StringBuilder message = new StringBuilder();
            message.append("The glowing zombie dropeed a plant food;");
            if (user.getPlantFoodCount() >= 3) {
                message.append("You already have 3 Plant Food.\n");
            } else {
                user.addPlantFoodCount(1);
                message.append(" you have ").append(user.getPlantFoodCount()).append(" plant foods now.\n");
            }
        }

        ZombieDrop type = getDropType();
        if (type == null) return;
        User user = App.getActiveUser();
        switch (type) {
            case POT -> {
                handlePotUnlock(user);
            }
            case DIAMOND -> {
                user.earnDiamond(1);
                GameSession.notify("A zombie dropped a diamond!");


            }
            case COIN -> {
                user.earnCoin(50);
                GameSession.notify("A zombie dropped 50 coins!");
            }
        }
    }

    private void handlePotUnlock(User user) {
        GreenHouse userGreenHouse = user.getGreenHouse();
        boolean unlockedOne = false;
        int totalUnlockedPots = 0;
        searchLoop:
        for (Pot[] pots : userGreenHouse.getPots()) {
            for (Pot pot : pots) {
                if (pot != null) {
                    if (pot.getPotCondition() != PotCondition.LOCKED) {
                        totalUnlockedPots++;
                    }
                    if (!unlockedOne && pot.getPotCondition() == PotCondition.LOCKED) {
                        pot.setPotCondition(PotCondition.EMPTY);
                        totalUnlockedPots++;
                        unlockedOne = true;
                        break searchLoop;
                    }
                }
            }
        }
        if (unlockedOne) {
            GameSession.notify("A zombie drop unlocked a new pot!");
        }
    }

    private ZombieDrop getDropType() {
        Random rand = new Random();
        int chance = rand.nextInt(10);
        if (chance == 9) {
            int choose = rand.nextInt(3);
            return switch (choose) {
                case 0 -> ZombieDrop.POT;
                case 1 -> ZombieDrop.DIAMOND;
                default -> ZombieDrop.COIN;
            };
        } else return null;
    }


    enum ZombieDrop {
        POT,
        DIAMOND,
        COIN
    }

}
