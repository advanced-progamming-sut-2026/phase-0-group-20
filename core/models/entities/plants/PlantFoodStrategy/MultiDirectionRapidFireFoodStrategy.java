package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.projectiles.ProjectileMechanism;

/**
 * Fires a rapid barrage in several fixed directions at once.
 * Used by: Rotobaga (4 diagonal directions), Starfruit (5-point star, including backward).
 */

public class MultiDirectionRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int durationTicks = 60;
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
            if (tickTimer % 2 == 0)
                ProjectileMechanism.executeNewProjectile(plant, true, true);

            if (tickTimer == 2)
                notify(plant.getName() + " unleashed a rapid barrage in all " + directionCount + " directions!");
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
