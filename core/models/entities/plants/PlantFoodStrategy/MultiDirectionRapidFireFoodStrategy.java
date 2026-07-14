package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.projectiles.ProjectileMechanism;
import models.game.GameSession;

/**
 * Fires a rapid barrage in several fixed directions at once.
 * Used by: Rotobaga (4 diagonal directions), Starfruit (5-point star, including backward).
 */

public class MultiDirectionRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int directionCount;
    private int tickTimer;

    public MultiDirectionRapidFireFoodStrategy(int directionCount) {
        this.directionCount = directionCount;
        this.tickTimer = 0;
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        if (tickTimer % 2 == 0)
            ProjectileMechanism.executeNewProjectile(plant, true, true);
        if (tickTimer == 2)
            System.out.println(plant.getName() + " unleashed a rapid barrage in all " + directionCount + " directions!");
    }
}
