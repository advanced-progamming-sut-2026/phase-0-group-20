package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

/**
 * Strike-Through Strategy:
 * Generates a projectile or laser beam that does not disappear upon hitting the first target.
 * It penetrates and deals damage to all enemies in a single line or specific range.
 */

public class StrikeThroughStrategy implements IPlantStrategy {
    private int lastShotTick = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * 10);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            // Logic to fire a penetrating projectile
            System.out.println(context.getName() + " fired a strike-through attack!");

            lastShotTick = currentTick;
        }
    }
}
