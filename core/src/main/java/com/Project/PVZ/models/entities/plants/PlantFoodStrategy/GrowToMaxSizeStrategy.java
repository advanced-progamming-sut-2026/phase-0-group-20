package com.Project.PVZ.models.entities.plants.PlantFoodStrategy;

import com.Project.PVZ.models.entities.plants.Plant;

public class GrowToMaxSizeStrategy implements PlantFoodStrategy {

    @Override
    public void executeStrategy(Plant plant) {
        plant.setSize(plant.getMaxSize());
        notify(plant.getName() + " grew to its maximum size!");
    }
}
