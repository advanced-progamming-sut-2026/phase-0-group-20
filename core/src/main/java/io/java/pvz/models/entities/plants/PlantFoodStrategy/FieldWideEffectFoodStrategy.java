package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.ProjectileMechanism;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.behavior.effect.ChillEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.FreezeEffect;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

public class FieldWideEffectFoodStrategy implements PlantFoodStrategy {

    private final String description;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean executed = false;

    public FieldWideEffectFoodStrategy(String description) {
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
            List<Zombie> allZombies = gameSession.getArena().getActiveZombies();
            String plantName = plant.getName().toLowerCase();

            int plantRow = plant.getPlacedTile().getRow();
            int plantCol = plant.getPlacedTile().getCol();

            switch (plantName) {
                case "iceberg lettuce":
                    for (Zombie zombie : allZombies)
                        if (!zombie.isDead())
                            if (gameSession.getCurrentChapter().getSeasonType() == SeasonType.FROZEN_CAVES)
                                zombie.addEffect(new ChillEffect(zombie, 15 * TimeManager.TICKS_PER_SECOND));
                            else
                                zombie.addEffect(new FreezeEffect(zombie, 15 * TimeManager.TICKS_PER_SECOND));
                    break;

                case "kernel-pult":
                    for (Zombie zombie : allZombies)
                        if (!zombie.isDead())
                            ProjectileMechanism.executeTargetedProjectile(plant, zombie, 0.5f);
                    break;

                case "garlic":
                    for (Zombie zombie : gameSession.getArena().zombieInRow(plantRow))
                        if (!zombie.isDead())
                            shiftZombieToAdjacentLane(zombie, gameSession);
                    break;

                case "sweet potato":
                    plant.setCurrentHp(plant.getBaseHp());
                    List<Zombie> nearby = gameSession.getArena().getZombiesInRadius(
                        plantCol, plantRow, PhysicalConstants.TILE_WIDTH * 2);
                    for (Zombie zombie : nearby)
                        if (!zombie.isDead() && zombie.getRow() != plantRow)
                            zombie.setRow(plantRow);
                    break;
            }
            executed = true;
        }
    }

    private void shiftZombieToAdjacentLane(Zombie zombie, GameSession gameSession) {
        int currentRow = zombie.getRow();
        int maxRows = gameSession.getArena().getRows();

        boolean canGoUp = (currentRow > 0);
        boolean canGoDown = (currentRow < maxRows - 1);

        if (canGoUp && canGoDown)
            zombie.setRow(currentRow - 1 + new Random().nextInt(2) * 2); //current row - 1 + (0 or 2)
        else if (canGoUp)
            zombie.setRow(currentRow - 1);
        else if (canGoDown)
            zombie.setRow(currentRow + 1);
        else
            zombie.setState(ZombieState.STUNNED);
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
