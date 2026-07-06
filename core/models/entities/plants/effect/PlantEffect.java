package models.entities.plants.effect;

import models.entities.plants.Plant;

public interface PlantEffect {
    void apply(Plant plant);
    void execute(Plant plant, int currentTick);
    void remove(Plant plant);
    boolean isExpired();
}
