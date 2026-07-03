package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;


/**
 * Area of Effect (AoE) Strategy:
 * Instead of firing a single projectile, the plant deals damage to all entities
 * within a specific geometric range (e.g., 3x3 grid around the plant or 3 tiles ahead).
 */

public class AoEStrategy implements IPlantStrategy {
    private int lastAttackTick = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * 10);

        if (intervalInTicks > 0 && (currentTick - lastAttackTick) >= intervalInTicks) {
            // Logic to calculate the AoE grid and damage multiple zombies at once
            System.out.println(context.getName() + " dealt AoE damage to surrounding area!");

            lastAttackTick = currentTick;
        }
    }
}
