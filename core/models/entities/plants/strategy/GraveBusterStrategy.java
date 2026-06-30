package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Grave Buster Strategy:
 * This plant can only be planted on a tile containing a Grave.
 * It takes a few seconds to consume the grave, destroying both the grave and itself.
 */

public class GraveBusterStrategy implements IPlantStrategy {
    private int startTick = -1;
    private final int BUSTING_DURATION_TICKS = 4 * 10;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) {
            startTick = currentTick;
            System.out.println(context.getName() + " started busting a grave!");
        }

        if (currentTick - startTick >= BUSTING_DURATION_TICKS) {
            System.out.println(context.getName() + " successfully destroyed the grave!");

            // Logic to remove the Grave object from the placed tile

            // The Grave Buster consumes itself in the process
            context.takeDamage(context.getCurrentHp());
        }
    }
}
