package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.List;

public class BurstEffectFoodStrategy implements PlantFoodStrategy {

    private final String description;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean executed = false;

    public BurstEffectFoodStrategy(String description) {
        this.description = description;
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
            String name = plant.getName().toLowerCase();
            int row = plant.getPlacedTile().getRow();
            int col = plant.getPlacedTile().getCol();
            int damage = 1800;//damage ziad haminghad khoobe Hossein?

            if (name.equalsIgnoreCase("fume-shroom")) {
                for (Zombie zombie : gameSession.getArena().zombieInRow(row)) {
                    if (!zombie.isDead() && zombie.getCol() >= col) {
                        zombie.takeDamage(damage);
                        if (zombie.isDead()) {
                            plant.onZombieDeath(zombie);
                        }
                        float pushBackDistance = PhysicalConstants.TILE_WIDTH * 3;
                        zombie.getPosition().moveX(pushBackDistance);
                        if (zombie.getCol() >= gameSession.getArena().getCols())
                            zombie.getPosition().setCol(gameSession.getArena().getCols() - 1);
                    }
                }
            } else {
                List<Zombie> nearZombies = gameSession.getArena().getZombiesInRadius(col, row, 1.5);
                for (Zombie zombie : nearZombies) {
                    if (zombie.isDead()) continue;
                    zombie.takeDamage(damage);
                    if (zombie.isDead()) {
                        plant.onZombieDeath(zombie);
                    }
                }
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
