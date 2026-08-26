package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.plants.strategy.tag_strategy.TrapStrategy;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.List;

public class CloneAndArmFoodStrategy implements PlantFoodStrategy {

    private final int cloneCount;
    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean executed = false;

    public CloneAndArmFoodStrategy(int cloneCount) {
        this.cloneCount = cloneCount;
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
            for (IPlantStrategy strategy : plant.getStrategies()) {
                if (strategy instanceof TrapStrategy trapStrategy) {
                    trapStrategy.setArmingTimeTicks(0);
                    trapStrategy.setArmed(true);
                }
            }

            List<Tile> randomEmptyTiles = GameSession.getInstance().getArena().getRandomEmptyTiles(cloneCount);

            if (randomEmptyTiles != null) {
                for (Tile tile : randomEmptyTiles) {
                    Plant newPlant = InGameEntityGenerator.getPlantForGame(plant, false);
                    newPlant.setPlacedTile(tile);
                    tile.addPlant(newPlant);
                    GameSession.getInstance().getArena().addPlant(newPlant);
                    GameSession.getInstance().getTimeManager().registerNewTicker(newPlant);

                    for (IPlantStrategy strategy : newPlant.getStrategies()) {
                        if (strategy instanceof TrapStrategy trapStrategy) {
                            trapStrategy.setArmingTimeTicks(0);
                            trapStrategy.setArmed(true);
                        }
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
