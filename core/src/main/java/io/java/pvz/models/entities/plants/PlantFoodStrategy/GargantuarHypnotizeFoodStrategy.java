package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;

public class GargantuarHypnotizeFoodStrategy implements PlantFoodStrategy {

    private boolean executed = false;
    private int setupTicks = 0;
    private int tickTimer = 0;

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;
        this.tickTimer = 0;

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        if (!executed && tickTimer > setupTicks) {
            plant.setBoosted(true);
            executed = true;
        }
    }

    @Override
    public int getDurationTicks() {
        return -1;
    }

    @Override
    public void reset() {
        this.executed = false;
        this.tickTimer = 0;
    }
}
