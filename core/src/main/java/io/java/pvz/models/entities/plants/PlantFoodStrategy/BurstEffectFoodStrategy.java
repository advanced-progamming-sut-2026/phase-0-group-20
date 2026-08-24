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
    private boolean executed = false;

    public BurstEffectFoodStrategy(String description) {
        this.description = description;
    }

    @Override
    public void onEnter(Plant plant) {
        PlantFoodStrategy.super.onEnter(plant);
        this.executed = false;

        float animDuration = 1.0f;
        AnimationCatalog.EntityAnimation anim = AnimationCatalog.getPlantAnimation(plant);
        if (anim != null) {
            if (anim.hasClip("plantfood_on")) animDuration = anim.getDuration("plantfood_on") +
                anim.getDuration("plantfood");
            else if (anim.hasClip("plantfood")) animDuration = anim.getDuration("plantfood");
        }
        this.durationTicks = (int) (animDuration * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void executeStrategy(Plant plant) {
        if (!executed) {
            GameSession gameSession = GameSession.getInstance();
            String name = plant.getName().toLowerCase();
            int row = plant.getPlacedTile().getRow();
            int col = plant.getPlacedTile().getCol();
            int damage = 1800; // damage ziad

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
    }
}
