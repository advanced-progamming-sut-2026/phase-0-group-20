package io.java.pvz.models.entities.plants.PlantFoodStrategy;

import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.LilyPadStrategy;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.fields.tiles.WaterTile;
import io.java.pvz.models.game.GameSession;

public class DuplicateSelfFoodStrategy implements PlantFoodStrategy {

    private int durationTicks = 0;
    private int setupTicks = 0;
    private int tickTimer = 0;
    private boolean executed = false;

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
            int row = plant.getPlacedTile().getRow();
            int col = plant.getPlacedTile().getCol();

            int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

            for (int[] dir : directions) {
                int newRow = row + dir[0];
                int newCol = col + dir[1];

                if (newRow >= 0 && newRow < gameSession.getArena().getRows() &&
                    newCol >= 0 && newCol < gameSession.getArena().getCols()) {

                    Tile targetTile = gameSession.getArena().getTile(newRow, newCol);

                    if (targetTile instanceof WaterTile) {
                        if (targetTile.getPlants().isEmpty()) {
                            Plant newLily = InGameEntityGenerator.getPlantForGame(plant, false);

                            newLily.setPlacedTile(targetTile);
                            targetTile.addPlant(newLily);
                            gameSession.getArena().addPlant(newLily);
                            gameSession.getTimeManager().registerNewTicker(newLily);

                        } else {
                            Plant p = targetTile.getPlants().get(0);
                            if (p.getStrategies().get(0) instanceof LilyPadStrategy)
                                p.setCurrentHp(p.getBaseHp());
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
