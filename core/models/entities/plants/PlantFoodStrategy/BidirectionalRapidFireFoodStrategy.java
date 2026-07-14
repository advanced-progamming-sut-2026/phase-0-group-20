package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.projectiles.ProjectileMechanism;
import models.game.GameSession;

/**
 * Fires a rapid barrage simultaneously forward and backward.
 * Used by: Split Pea.
 */

public class BidirectionalRapidFireFoodStrategy implements PlantFoodStrategy {

    private int tickTimer = 0;

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        GameSession session = GameSession.getInstance();

        if (tickTimer % 2 == 0)
            ProjectileMechanism.executeNewProjectile(plant,  true, false); //khodesh be aghab ham shelik mikone

        if (tickTimer == 2)
            System.out.println(plant.getName() + " fired a rapid barrage forward AND backward!");

    }
}