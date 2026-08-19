package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.FreezeEffect;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

/**
 * Instantly freezes every zombie currently in the lane, then fires a rapid
 * barrage of icy projectiles down it.
 * Used by: Snow Pea.
 */

public class IcyRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int durationTicks = 6 * TimeManager.TICKS_PER_SECOND;
    private int tickTimer;

    public IcyRapidFireFoodStrategy() {
        tickTimer = 0;
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        GameSession gameSession = GameSession.getInstance();

        if (tickTimer <= durationTicks) {
            if (tickTimer == 1) {
                int row = plant.getPlacedTile().getRow();

                for (Zombie zombie : gameSession.getArena().zombieInRow(row))
                    if (!zombie.isDead())
                        zombie.addEffect(new FreezeEffect(zombie, 15 * TimeManager.TICKS_PER_SECOND));
            }

            if (tickTimer % 2 == 0)
                ProjectileMechanism.executeNewProjectile(plant, true, false, 0.1f);

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
