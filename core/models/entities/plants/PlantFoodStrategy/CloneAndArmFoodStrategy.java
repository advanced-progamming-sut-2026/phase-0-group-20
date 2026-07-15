package models.entities.plants.PlantFoodStrategy;

import models.InGameEntityGenerator;
import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.plants.strategy.tag_strategy.TrapStrategy;
import models.fields.tiles.Tile;
import models.game.GameSession;

import java.util.List;

/**
 * Instantly arms the plant (skipping the normal arm-time delay) and throws
 * a number of cloned copies of itself onto other random tiles, already armed.
 * Used by: Potato Mine, Primal Potato Mine (2 clones each).
 */

public class CloneAndArmFoodStrategy implements PlantFoodStrategy {

    private final int cloneCount;

    public CloneAndArmFoodStrategy(int cloneCount) {
        this.cloneCount = cloneCount;
    }

    @Override
    public void executeStrategy(Plant plant) {

        for (IPlantStrategy strategy : plant.getStrategies())
            if (strategy instanceof TrapStrategy trapStrategy) {
                trapStrategy.setArmingTimeTicks(0);
                trapStrategy.setArmed(true);
            }

        List<Tile> randomEmptyTiles = GameSession.getInstance().getArena().getRandomEmptyTiles(cloneCount);

        if (randomEmptyTiles != null) {
            for (Tile tile : randomEmptyTiles) {

                Plant newPlant = InGameEntityGenerator.getPlantForGame(plant, true);

                newPlant.setPlacedTile(tile);
                tile.addPlant(newPlant);

                for (IPlantStrategy strategy : newPlant.getStrategies()) {
                    if (strategy instanceof TrapStrategy trapStrategy) {
                        trapStrategy.setArmingTimeTicks(0);
                        trapStrategy.setArmed(true);
                    }
                }
            }
        }

        System.out.println(plant.getName() + " instantly armed itself and threw " + cloneCount + " clone(s) onto the field!");
    }
}
