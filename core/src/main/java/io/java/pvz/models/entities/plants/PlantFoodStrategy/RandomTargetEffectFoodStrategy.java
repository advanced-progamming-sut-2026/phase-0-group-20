package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


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

        for (int i = 0; i < pool.size(); i++) {
            int rnd = new Random().nextInt(pool.size());
            targets.add(pool.get(rnd));
            pool.remove(rnd);
            if (targets.size() >= hits) break;
        }

        if (targets.isEmpty()) {
            notify(plant.getName() + " found no zombies to target with its Plant Food effect!");
            return;
        }

        boolean isLobbedProjectile = plant.getName().equalsIgnoreCase("Cabbage-pult")
                || plant.getName().equalsIgnoreCase("Melon-pult")
                || plant.getName().equalsIgnoreCase("Winter Melon")
                || plant.getName().equalsIgnoreCase("Pepper-pult")
                || plant.getName().equalsIgnoreCase("Bowling Bulb");

        for (Zombie target : targets) {
            if (isLobbedProjectile)
                ProjectileMechanism.executeTargetedProjectile(plant, target, 1);
            else
                applyDirectEffect(target, plant);
        }

        notify(plant.getName() + " targeted " + hits + " random zombie(s) and applied effect: " + effectDescription);
    }

    private void applyDirectEffect(Zombie target, Plant plant) {
        switch (plant.getName().toLowerCase()) {
            case "electric blueberry", "tangle kelp", "chomper" -> {
                target.takeDamage(10000); //yahtamel plant be kar biad
                if (target.isDead()) {
                    plant.onZombieDeath(target);
                }
            }
            case "squash" -> {
                int damage = plant.getDamage();
                target.takeDamage(damage);
                if (target.isDead()) {
                    plant.onZombieDeath(target);
                }
            }
            case "caulipower" -> {
                target.hypnotize();
                notify(target.getName() + " was hypnotized!");
            }
            default -> notify(target.getName() + " was hit by an unmapped random-target Plant Food effect.");
        }
    }
}
