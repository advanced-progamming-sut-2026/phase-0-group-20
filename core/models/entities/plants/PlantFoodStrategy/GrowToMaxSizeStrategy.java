package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

public class GrowToMaxSizeStrategy implements PlantFoodStrategy {

    @Override
    public void executeStrategy(Plant plant) {
        plant.setSize(plant.getMaxSize());
        System.out.println(plant.getName() + " grew to its maximum size!");
    }
}
