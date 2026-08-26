package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public interface PlantFoodStrategy {

    default void onEnter(Plant plant) {
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null) {
            if (anim.hasClip("plantfood_on")) {
                plant.triggerAction("plantfood_on");
            } else if (anim.hasClip("plantfood_stage" + plant.getSize())) {
                plant.triggerAction("plantfood_stage" + plant.getSize());
            } else {
                plant.triggerAction("plantfood");
            }
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

    default int[] calculateTimings(Plant plant) {
        float animDuration = 0f;
        float setupDuration = 0f;
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);

        if (anim != null) {
            boolean hasOn = anim.hasClip("plantfood_on");
            boolean hasStage = anim.hasClip("plantfood_stage" + plant.getSize());
            boolean hasLoop = anim.hasClip("plantfood");

            float loopLen = 0f;
            if (hasStage) {
                loopLen = anim.getDuration("plantfood_stage" + plant.getSize());
            } else if (hasLoop) {
                loopLen = anim.getDuration("plantfood");
            }

            if (hasOn && loopLen > 0) {
                setupDuration = anim.getDuration("plantfood_on");
                animDuration = setupDuration + loopLen;
            } else if (loopLen > 0) {
                setupDuration = 0f;
                animDuration = loopLen;
            } else if (hasOn) {
                setupDuration = 0f;
                animDuration = anim.getDuration("plantfood_on");
            }
        }

        return new int[]{
            (int) (setupDuration * TimeManager.TICKS_PER_SECOND),
            (int) (animDuration * TimeManager.TICKS_PER_SECOND)
        };
    }

    default int getDurationTicks() {
        return 0;
    }

    default void reset() {
    }
}
