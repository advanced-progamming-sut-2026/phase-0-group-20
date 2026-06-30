package models.entities.plants.strategy.category_strategy;


import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

/**
 * Homing Strategy:
 * This DigestionStrategy scans the entire board (or specific lanes) to find the nearest zombie.
 * Instead of shooting straight, it locks onto a target and fires a homing projectile.
 */

public class HomingStrategy implements IPlantStrategy {
    private int lastShotTick = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * 10);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            // Logic to find the nearest zombie on the entire board
            System.out.println(context.getName() + " locked onto a target and fired a homing projectile!");

            lastShotTick = currentTick;
        }
    }
}
