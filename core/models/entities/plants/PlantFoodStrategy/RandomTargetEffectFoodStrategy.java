package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;

/**
 * Generic "pick N random zombies (on the board, in the water, or on the
 * ground depending on the plant) and apply an effect to each" strategy.
 * This single parametrized class backs many different plants whose Plant
 * Food effect follows the same "select random targets -> hit them" shape:
 * <p>
 * - Caulipower: hypnotize a few random zombies          -> (count, "hypnotized")
 * - Electric Blueberry: instantly destroy 3 random zombies -> (3, "destroyed")
 * - Squash: crush 2 random zombies on the ground          -> (2, "crushed")
 * - Tangle Kelp: drag several random zombies underwater   -> (count, "dragged underwater")
 * - Bowling Bulb: lob 3 giant exploding onions             -> (3, "exploding onion")
 * - Cabbage-pult / Melon-pult / Winter Melon / Pepper-pult: lob projectile at random zombies
 * - Chomper: instantly swallow 3 zombies from range        -> (3, "swallowed")
 */

public class RandomTargetEffectFoodStrategy implements PlantFoodStrategy {

    private final int targetCount;
    private final String effectDescription;

    public RandomTargetEffectFoodStrategy(int targetCount, String effectDescription) {
        this.targetCount = targetCount;
        this.effectDescription = effectDescription;
    }

    @Override
    public void executeStrategy(Plant plant) {
        System.out.println(plant.getName() + " targeted " + targetCount + " random zombie(s) and applied effect: " + effectDescription);
    }
}
