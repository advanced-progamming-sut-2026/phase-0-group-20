package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.timeManager.TimeManager;

/**
 * Fires a rapid barrage in several fixed directions at once.
 * Used by: Rotobaga (4 diagonal directions), Starfruit (5-point star, including backward).
 */

public class MultiDirectionRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int durationTicks = 6 * TimeManager.TICKS_PER_SECOND;
    private final int directionCount;
    private int tickTimer;

    public MultiDirectionRapidFireFoodStrategy(int directionCount) {
        this.directionCount = directionCount;
        this.tickTimer = 0;
    }

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
