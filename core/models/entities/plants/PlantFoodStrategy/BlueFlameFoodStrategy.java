package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.TorchwoodStrategy;

/**
 * Creates a "blue flame": every projectile that passes through this
 * Torchwood now gets a x3 damage multiplier instead of the normal x2.
 * Used by: Torchwood.
 */

public class BlueFlameFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " ignited a blue flame - passing projectiles now deal 3x damage!");
        plant.getStrategies().stream()
                .filter(s -> s instanceof TorchwoodStrategy)
                .map(s -> (TorchwoodStrategy) s)
                .findFirst()
                .ifPresent(TorchwoodStrategy::activateBlueFlame);
    }
}