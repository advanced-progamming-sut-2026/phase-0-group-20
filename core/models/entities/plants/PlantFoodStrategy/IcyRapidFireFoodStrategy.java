package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.projectiles.ProjectileMechanism;
import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.effect.FreezeEffect;
import models.game.GameSession;

/**
 * Instantly freezes every zombie currently in the lane, then fires a rapid
 * barrage of icy projectiles down it.
 * Used by: Snow Pea.
 */

public class IcyRapidFireFoodStrategy implements PlantFoodStrategy {

    private int tickTimer;

    public IcyRapidFireFoodStrategy() {
        tickTimer = 0;
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        GameSession gameSession = GameSession.getInstance();

        if (tickTimer == 1) {
            int row = plant.getPlacedTile().getRow();

            for (Zombie zombie : gameSession.getArena().zombieInRow(row))
                if (!zombie.isDead())
                    zombie.addEffect(new FreezeEffect(zombie, 150));
        }

        if (tickTimer % 2 == 0)
            ProjectileMechanism.executeNewProjectile(plant, gameSession, true, false);

        if (tickTimer == 2)
            System.out.println(plant.getName() + " froze the entire lane and unleashed an icy barrage!");

    }

}
