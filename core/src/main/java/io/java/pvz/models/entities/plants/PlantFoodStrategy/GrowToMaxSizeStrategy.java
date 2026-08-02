package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;

public class GrowToMaxSizeStrategy implements PlantFoodStrategy {

    @Override
    public void executeStrategy(Plant plant) {
        plant.setSize(plant.getMaxSize());
        notify(plant.getName() + " grew to its maximum size!");
    }
}
