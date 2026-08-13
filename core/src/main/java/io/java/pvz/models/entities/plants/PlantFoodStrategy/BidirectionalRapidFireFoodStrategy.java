package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;

/**
 * Fires a rapid barrage simultaneously forward and backward.
 * Used by: Split Pea.
 */

public class BidirectionalRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int durationTicks = 60;
    private int tickTimer = 0;

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (tickTimer <= durationTicks) {

            if (tickTimer % 2 == 0)
                ProjectileMechanism.executeNewProjectile(plant, true, true, 1);

            if (tickTimer == 2)
                notify(plant.getName() + " fired a rapid barrage forward AND backward!");
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
