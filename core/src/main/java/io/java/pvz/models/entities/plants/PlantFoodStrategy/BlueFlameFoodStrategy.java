package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.TorchwoodStrategy;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class BlueFlameFoodStrategy implements PlantFoodStrategy {

    private boolean executed = false;

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;
    }

    @Override
    public void executeStrategy(Plant plant) {
        if (!executed) {
            plant.getStrategies().stream()
                .filter(s -> s instanceof TorchwoodStrategy)
                .map(s -> (TorchwoodStrategy) s)
                .findFirst()
                .ifPresent(TorchwoodStrategy::activateBlueFlame);
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
    }
}
