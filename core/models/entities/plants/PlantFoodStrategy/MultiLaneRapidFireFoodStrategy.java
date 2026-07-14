package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.projectiles.ProjectileMechanism;
import models.game.GameSession;

/**
 * Fires a fan-shaped rapid barrage across all of this plant's lanes at once.
 * Used by: Threepeater (3 lanes).
 */

public class MultiLaneRapidFireFoodStrategy implements PlantFoodStrategy {
    private int tickTimer = 0;

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (tickTimer % 2 == 0) {
            ProjectileMechanism.executeNewProjectile(plant, GameSession.getInstance(), true, false);
            System.out.println(plant.getName() + " fired a fan-shaped rapid barrage across all its lanes!");
        }
    }
}
