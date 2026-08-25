package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class BidirectionalRapidFireFoodStrategy implements PlantFoodStrategy {

    private final int minDuration = 6 * TimeManager.TICKS_PER_SECOND;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.tickTimer = 0;

        float animDuration = 0;
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
        this.durationTicks = Math.max(minDuration + setupTicks, (int) (animDuration * TimeManager.TICKS_PER_SECOND));
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;

        if (tickTimer > setupTicks && tickTimer <= durationTicks) {
            int activeTick = tickTimer - setupTicks;

            if (activeTick % (TimeManager.TICKS_PER_SECOND / 5) == 0)
                ProjectileMechanism.executeNewProjectile(plant, true, true, 0.1f);
        }
    }

    @Override
    public int getDurationTicks() {
        return durationTicks;
    }

    @Override
    public void reset() {
        this.tickTimer = 0;
    }
}
