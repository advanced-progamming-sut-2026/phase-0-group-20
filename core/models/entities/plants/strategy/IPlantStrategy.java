package models.entities.plants.strategy;

import models.entities.plants.Plant;

public interface IPlantStrategy {
    void execute(Plant context, int currentTick);
}
