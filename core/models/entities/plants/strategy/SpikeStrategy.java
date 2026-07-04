package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Spike Strategy:
 * Plants with this strategy cannot be eaten by normal walking zombies.
 * Instead, they deal continuous physical damage to any zombie stepping on their tile.
 */

public class SpikeStrategy implements IPlantStrategy {
    private final int DAMAGE_INTERVAL = 5;
    private int lastDamageTick = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        if (currentTick - lastDamageTick >= DAMAGE_INTERVAL) {
            // Logic to get all zombies currently standing on this plant's tile

            // For each zombie, apply damage
            System.out.println(context.getName() + " is damaging zombies stepping on it!");

            lastDamageTick = currentTick;
        }
    }
}
