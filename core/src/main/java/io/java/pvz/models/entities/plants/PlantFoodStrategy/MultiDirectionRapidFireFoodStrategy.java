package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.timeManager.TimeManager;

public class MultiDirectionRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int minDuration = 6 * TimeManager.TICKS_PER_SECOND;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private final int directionCount;
    private int tickTimer;

    public MultiDirectionRapidFireFoodStrategy(int directionCount) {
        this.directionCount = directionCount;
        this.tickTimer = 0;
    }

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.tickTimer = 0;

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];
        this.durationTicks = Math.max(minDuration + setupTicks, timings[1]);
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (tickTimer > setupTicks && tickTimer <= durationTicks) {
            int activeTick = tickTimer - setupTicks;

            if (activeTick % (TimeManager.TICKS_PER_SECOND / 5) == 0)
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
