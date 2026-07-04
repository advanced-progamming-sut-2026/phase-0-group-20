package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;


/**
 * Trap Strategy:
 * The plant remains inactive (unarmed) for a specific duration after planting.
 * Once armed, it monitors its tile (or adjacent ones) and triggers a deadly effect
 * when an enemy steps on it.
 */

public class TrapStrategy implements IPlantStrategy {
    private final int ARMING_TIME_TICKS = 14 * 10;
    private int startTick = -1;
    private boolean isArmed = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) startTick = currentTick;

        if (!isArmed && (currentTick - startTick) >= ARMING_TIME_TICKS) {
            isArmed = true;
            System.out.println(context.getName() + " is now armed and ready!");
        }

        if (isArmed) {
            // Logic to check collision with zombies on this tile
            // If zombie detected -> Explode and kill the trap plant
        }
    }
}
