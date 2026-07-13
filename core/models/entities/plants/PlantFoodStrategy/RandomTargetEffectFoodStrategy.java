package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.projectiles.ProjectileMechanism;
import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        GameSession gameSession = GameSession.getInstance();

        List<Zombie> pool = new ArrayList<>(gameSession.getArena().getActiveZombies());
        pool.removeIf(Zombie::isDead);

        int hits = Math.min(targetCount, pool.size());
        List<Zombie> targets = new ArrayList<>();

        for (int i = 0 ; i < pool.size(); i++) {
            int rnd = new Random().nextInt(pool.size());
            targets.add(pool.get(rnd));
            pool.remove(rnd);
            if (targets.size() >= hits) break;
        }

        if (targets.isEmpty()) {
            System.out.println(plant.getName() + " found no zombies to target with its Plant Food effect!");
            return;
        }

        boolean isLobbedProjectile = plant.getName().equalsIgnoreCase("Cabbage-pult")
                || plant.getName().equalsIgnoreCase("Melon-pult")
                || plant.getName().equalsIgnoreCase("Winter Melon")
                || plant.getName().equalsIgnoreCase("Pepper-pult")
                || plant.getName().equalsIgnoreCase("Bowling Bulb");

        for (Zombie target : targets) {
            if (isLobbedProjectile)
                ProjectileMechanism.executeTargetedProjectile(plant, gameSession, target, 0);
            else
                applyDirectEffect(target, plant);
        }

        System.out.println(plant.getName() + " targeted " + hits + " random zombie(s) and applied effect: " + effectDescription);
    }

    private void applyDirectEffect(Zombie target, Plant plant) {
        switch (plant.getName().toLowerCase()) {
            case "electric blueberry", "tangle kelp", "chomper" ,"squash"->
                    target.takeDirectDamage(10000, plant); //yahtamel plant be kar biad
            case "caulipower" -> {
                target.hypnotize();
                System.out.println(target.getName() + " was hypnotized!");
            }
            default ->
                    System.out.println(target.getName() + " was hit by an unmapped random-target Plant Food effect.");
        }
    }
}
