package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.TorchwoodStrategy;

public class BlueFlameFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        plant.getStrategies().stream()
                .filter(s -> s instanceof TorchwoodStrategy)
                .map(s -> (TorchwoodStrategy) s)
                .findFirst()
                .ifPresent(TorchwoodStrategy::activateBlueFlame);
    }
}
