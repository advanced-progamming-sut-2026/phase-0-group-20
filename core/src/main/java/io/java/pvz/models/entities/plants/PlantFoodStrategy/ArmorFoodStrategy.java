package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.plants.strategy.SpikeStrategy;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

public class ArmorFoodStrategy implements PlantFoodStrategy {

    private final int armorAmount;
    private final boolean boostsReflectDamage;
    private int durationTicks = 0;
    private boolean executed = false;

    public ArmorFoodStrategy(int armorAmount) {
        this(armorAmount, false);
    }

    public ArmorFoodStrategy(int armorAmount, boolean boostsReflectDamage) {
        this.armorAmount = armorAmount;
        this.boostsReflectDamage = boostsReflectDamage;
    }

    @Override
    public void executeStrategy(Plant plant) {
        int maxHpWithArmor = plant.getBaseHp() + armorAmount;
        plant.setCurrentHp(maxHpWithArmor);

        if (boostsReflectDamage) {
            for (IPlantStrategy strategy : plant.getStrategies()) {
                if (strategy instanceof SpikeStrategy spikeStrategy) {
                    spikeStrategy.setHasArmor(true);
                }
            }
        }

        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null) {
            if (anim.hasClip("plantfood_on")) {
                plant.triggerAction("plantfood_on");
            } else if (anim.hasClip("idle_plantfood")) {
                plant.triggerAction("idle_plantfood");
            } else if (anim.hasClip("plantfood")) {
                plant.triggerAction("plantfood");
            }
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;

        float animDuration = 1.0f;
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null) {
            if (anim.hasClip("plantfood_on")) animDuration = anim.getDuration("plantfood_on") +
                anim.getDuration("plantfood");
            else if (anim.hasClip("plantfood")) animDuration = anim.getDuration("plantfood");
        }
        this.durationTicks = (int) (animDuration * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void executeStrategy(Plant plant) {
        if (!executed) {
            int maxHpWithArmor = plant.getBaseHp() + armorAmount;
            plant.setCurrentHp(maxHpWithArmor);

            if (boostsReflectDamage) {
                for (IPlantStrategy strategy : plant.getStrategies())
                    if (strategy instanceof SpikeStrategy spikeStrategy)
                        spikeStrategy.setHasArmor(true);
            }
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
