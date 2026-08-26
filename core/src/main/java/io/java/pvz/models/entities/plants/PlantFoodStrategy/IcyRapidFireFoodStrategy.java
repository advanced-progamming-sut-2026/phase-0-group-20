package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.FreezeEffect;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class IcyRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int minDuration = 6 * TimeManager.TICKS_PER_SECOND;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean frozenExecuted = false;

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.tickTimer = 0;
        this.frozenExecuted = false;

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];
        this.durationTicks = Math.max(minDuration + setupTicks, timings[1]);
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (tickTimer > setupTicks && tickTimer <= durationTicks) {

            if (!frozenExecuted) {
                int row = plant.getPlacedTile().getRow();
                for (Zombie zombie : GameSession.getInstance().getArena().zombieInRow(row))
                    if (!zombie.isDead())
                        zombie.addEffect(new FreezeEffect(zombie, 15 * TimeManager.TICKS_PER_SECOND));
                frozenExecuted = true;
            }

            int activeTick = tickTimer - setupTicks;
            if (activeTick % (TimeManager.TICKS_PER_SECOND / 5) == 0)
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
        this.frozenExecuted = false;
    }

}
