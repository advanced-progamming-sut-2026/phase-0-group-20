package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class SunBurstFoodStrategy implements PlantFoodStrategy {
    private final int sunAmount;
    private int durationTicks = 0;
    private boolean executed = false;

    public SunBurstFoodStrategy(int sunAmount) {
        this.sunAmount = sunAmount;
    }

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;

        float animDuration = 0f;
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null) {
            if (anim.hasClip("plantfood")) {
                animDuration = anim.getDuration("plantfood");
            } else if (anim.hasClip("plantfood_stage" + plant.getSize())) {
                animDuration = anim.getDuration("plantfood_stage" + plant.getSize());
            }
        }

        this.durationTicks = Math.max(1, (int) (animDuration * TimeManager.TICKS_PER_SECOND));
    }

    @Override
    public void executeStrategy(Plant plant) {
        if (!executed) {
            GameSession.getInstance().addSun(sunAmount);
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
    }
}
