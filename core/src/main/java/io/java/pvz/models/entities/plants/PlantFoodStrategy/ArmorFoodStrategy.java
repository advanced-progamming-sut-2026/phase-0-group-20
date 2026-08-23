package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.plants.strategy.SpikeStrategy;
import io.java.pvz.utils.AnimationCatalog;

public class ArmorFoodStrategy implements PlantFoodStrategy {

    private final int armorAmount;
    private final boolean boostsReflectDamage;

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
        }
    }
}
