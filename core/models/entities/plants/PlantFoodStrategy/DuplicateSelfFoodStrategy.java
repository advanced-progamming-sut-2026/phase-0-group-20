package models.entities.plants.PlantFoodStrategy;

import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.entities.plants.strategy.LifespanStrategy;
import models.entities.plants.strategy.LilyPadStrategy;
import models.fields.tiles.Tile;
import models.fields.tiles.WaterTile;
import models.game.GameSession;

/**
 * Spawns several free copies of this plant on other empty water tiles.
 * Used by: Lily Pad.
 */

public class DuplicateSelfFoodStrategy implements PlantFoodStrategy {
    @Override
    public void executeStrategy(Plant plant) {
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
                    if (targetTile.getPlants().isEmpty()){
                        // spawn a lily pad here
                    } else {
                        Plant p = targetTile.getPlants().get(0);
                        if (p.getStrategies().get(0) instanceof LilyPadStrategy)
                            p.setCurrentHp(p.getBaseHp());
                    }
                }
            }
        }

        System.out.println(plant.getName() + " spawned extra copies of itself on nearby empty water tiles!");
    }
}
