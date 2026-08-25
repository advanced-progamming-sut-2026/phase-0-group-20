package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomTargetEffectFoodStrategy implements PlantFoodStrategy {

    private final int targetCount;
    private final String effectDescription;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean executed = false;

    public RandomTargetEffectFoodStrategy(int targetCount, String effectDescription) {
        this.targetCount = targetCount;
        this.effectDescription = effectDescription;
    }

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;
        this.tickTimer = 0;

        float animDuration = 1.0f;
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
        this.durationTicks = (int) (animDuration * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void executeStrategy(Plant plant) {
        tickTimer++;
        if (!executed && tickTimer > setupTicks) {
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

            boolean isLobbedProjectile = plant.getName().equalsIgnoreCase("Cabbage-pult")
                || plant.getName().equalsIgnoreCase("Melon-pult")
                || plant.getName().equalsIgnoreCase("Winter Melon")
                || plant.getName().equalsIgnoreCase("Pepper-pult")
                || plant.getName().equalsIgnoreCase("Bowling Bulb");

            for (Zombie target : targets) {
                if (isLobbedProjectile)
                    ProjectileMechanism.executeTargetedProjectile(plant, target, 0.1f);
                else
                    applyDirectEffect(target, plant);
            }
            executed = true;
        }
    }

    private void applyDirectEffect(Zombie target, Plant plant) {
        switch (plant.getName().toLowerCase()) {
            case "electric blueberry", "tangle kelp", "chomper" -> {
                target.takeDamage(10000);
                if (target.isDead()) plant.onZombieDeath(target);
            }
            case "squash" -> {
                target.takeDamage(plant.getDamage());
                if (target.isDead()) plant.onZombieDeath(target);
            }
            case "caulipower" -> target.hypnotize();
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
