package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.plants.strategy.SpikeStrategy;

public class ArmorFoodStrategy implements PlantFoodStrategy {

    private final int armorAmount;
    private final boolean boostsReflectDamage;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean executed = false;

    public ArmorFoodStrategy(int armorAmount) {
        this(armorAmount, false);
    }

    public ArmorFoodStrategy(int armorAmount, boolean boostsReflectDamage) {
        this.armorAmount = armorAmount;
        this.boostsReflectDamage = boostsReflectDamage;
    }

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;
        this.tickTimer = 0;

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];
        this.durationTicks = Math.max(1, timings[1]);
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        if (!executed && tickTimer > setupTicks) {
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
        this.tickTimer = 0;
    }
}
