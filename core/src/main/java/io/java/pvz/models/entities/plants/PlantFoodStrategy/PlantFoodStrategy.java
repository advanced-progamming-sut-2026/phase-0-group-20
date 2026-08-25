package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.utils.AnimationCatalog;

public interface PlantFoodStrategy {

    default void onEnter(Plant plant) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null && anim.hasClip("plantfood_on")) {
            plant.triggerAction("plantfood_on");
        } else {
            plant.triggerAction("plantfood");
        }
    }

    void executeStrategy(Plant plant);

    default void onExit(Plant plant) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null && anim.hasClip("plantfood_off")) {
            plant.triggerAction("plantfood_off");
        }
    }

    default int getDurationTicks() {
        return 0;
    }

    default void reset() {
    }
}
