package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Grants the plant permanent bonus "armor" HP (on top of its current HP) and,
 * optionally, a combat buff.
 * Used by: Wall-nut (+4000 armor), Tall-nut (+8000 armor), Endurian (metal
 * armor + increased reflect damage), Explode-o-nut (metal armor, also
 * explodes when the armor itself is destroyed), Pumpkin (strong metal
 * armor), Sun Bean (strong metal armor).
 */

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
        System.out.println(plant.getName() + " gained " + armorAmount + " permanent armor!");
        if (boostsReflectDamage) {
            System.out.println(plant.getName() + " also gained increased damage reflection!");
        }
    }
}