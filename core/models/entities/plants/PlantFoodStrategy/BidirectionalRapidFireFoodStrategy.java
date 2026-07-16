package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.projectiles.ProjectileMechanism;
import models.game.GameSession;

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
                ProjectileMechanism.executeNewProjectile(plant, true, false); //khodesh be aghab ham shelik mikone

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