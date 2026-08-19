package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.TorchwoodStrategy;

/**
 * Creates a "blue flame": every projectile that passes through this
 * Torchwood now gets a x3 damage multiplier instead of the normal x2.
 * Used by: Torchwood.
 */

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
