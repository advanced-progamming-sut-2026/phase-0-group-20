package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Used for plants whose CSV "Plant Food Effect" column is "ندارد (مصرفی آنی)":
 * one-shot consumables (Cherry Bomb, Jalapeno, Doom-shroom, Ice-shroom, Hot Potato,
 * Grave Buster, Grapeshot, Gold Bloom) and Imitater (effect depends on the copied
 * plant, handled elsewhere) and the "-mint" family (the mint itself IS the plant
 * food application - see MintBuffStrategy/GlobalEffectStrategy in the regular
 * strategy package, not here).
 * Feeding these plants normally shouldn't even be possible in-game (no PF icon),
 * this class just guarantees nothing crashes if it's attempted anyway.
 */

public class NoFoodEffectStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " has no Plant Food effect (one-shot/consumable plant).");
    }
}
