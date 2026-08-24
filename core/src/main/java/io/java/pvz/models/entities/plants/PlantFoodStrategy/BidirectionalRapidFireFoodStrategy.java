package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.timeManager.TimeManager;

/**
 * Fires a rapid barrage simultaneously forward and backward.
 * Used by: Split Pea.
 */

public class BidirectionalRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int durationTicks = 6 * TimeManager.TICKS_PER_SECOND;
    private int tickTimer = 0;

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (tickTimer <= durationTicks) {

            if (tickTimer % (TimeManager.TICKS_PER_SECOND / 5) == 0)
                ProjectileMechanism.executeNewProjectile(plant, true, true, 0.1f);
        }
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void reset() {
        this.tickTimer = 0;
    }

}
