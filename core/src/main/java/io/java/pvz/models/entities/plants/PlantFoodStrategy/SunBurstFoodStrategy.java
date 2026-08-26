package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.GameSession;

public class SunBurstFoodStrategy implements PlantFoodStrategy {
    private final int sunAmount;
    private int durationTicks = 0;
    private boolean executed = false;
    private int setupTicks = 0;
    private int tickTimer = 0;

    public SunBurstFoodStrategy(int sunAmount) {
        this.sunAmount = sunAmount;
    }

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;
        this.tickTimer = 0;

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];
        this.durationTicks = Math.max(1, timings[1]);
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        if (!executed && tickTimer > setupTicks) {
            GameSession.getInstance().addSun(sunAmount);
            executed = true;
        }
    }


    @Override
    public int getDurationTicks() {
        return durationTicks;
    }


    @Override
    public void reset() {
        this.executed = false;
        this.tickTimer = 0;
    }

}
