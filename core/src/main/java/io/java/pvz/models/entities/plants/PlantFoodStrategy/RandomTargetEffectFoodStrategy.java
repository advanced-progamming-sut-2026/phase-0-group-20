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

        int[] timings = calculateTimings(plant);
        this.setupTicks = timings[0];

        this.durationTicks = Math.max(1, timings[1]);
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

            for (int i = 0; i < hits; i++) {
                if (pool.isEmpty()) break;
                int rnd = new Random().nextInt(pool.size());
                targets.add(pool.get(rnd));
                pool.remove(rnd);
            }

            String pName = plant.getName().toLowerCase();
            boolean firesTargetedProjectile = pName.equals("cabbage-pult")
                || pName.equals("melon-pult")
                || pName.equals("winter melon")
                || pName.equals("pepper-pult")
                || pName.equals("bowling bulb")
                || pName.equals("electric blueberry")
                || pName.equals("caulipower");

            for (Zombie target : targets) {
                if (firesTargetedProjectile) {

                    int pfDamage = plant.getDamage() * 20;
                    if (pName.equals("electric blueberry"))
                        pfDamage = 99999;

                    ProjectileMechanism.executeTargetedProjectile(plant, target, 0.1f, pfDamage);

                } else
                    applyDirectEffect(target, plant);
            }
            executed = true;
        }
    }

    private void applyDirectEffect(Zombie target, Plant plant) {
        switch (plant.getName().toLowerCase()) {
            case "tangle kelp", "chomper" -> {
                target.takeDamage(10000);
                if (target.isDead()) plant.onZombieDeath(target);
            }
            case "squash" -> {
                target.takeDamage(plant.getDamage());
                if (target.isDead()) plant.onZombieDeath(target);
            }
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
