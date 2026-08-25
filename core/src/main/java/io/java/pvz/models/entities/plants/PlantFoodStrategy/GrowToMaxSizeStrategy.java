package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class GrowToMaxSizeStrategy implements PlantFoodStrategy {

    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean executed = false;

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;
        this.tickTimer = 0;

        float animDuration = 1.0f;
        float setupDuration = 0f;
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null) {
            if (anim.hasClip("plantfood_on")) {
                setupDuration = anim.getDuration("plantfood_on");
                animDuration = setupDuration + anim.getDuration("plantfood");
            } else if (anim.hasClip("plantfood")) {
                animDuration = anim.getDuration("plantfood");
            }
        }
        this.setupTicks = (int) (setupDuration * TimeManager.TICKS_PER_SECOND);
        this.durationTicks = (int) (animDuration * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        if (!executed && tickTimer > setupTicks) {
            plant.setSize(plant.getMaxSize());
            executed = true;
        }
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void reset() {
        this.executed = false;
        this.tickTimer = 0;
    }
}
