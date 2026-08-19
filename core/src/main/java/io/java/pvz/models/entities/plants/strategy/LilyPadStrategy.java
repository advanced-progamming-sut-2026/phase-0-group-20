package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;

public class LilyPadStrategy implements IPlantStrategy {
    private boolean isSinking = false;
    @Override
    public void execute(Plant context, int currentTick) {
        if ((context.getCurrentHp() <= 0 || context.isDead()) && !isSinking) {
            isSinking = true;
            Tile tile = context.getPlacedTile();
            if (tile != null) {
                for (Plant stackedPlant : tile.getPlants()) {
                    if (stackedPlant != context && !stackedPlant.isDead()) {
                        stackedPlant.takeDamage(stackedPlant.getCurrentHp());
                    }
                }
            }
        }
    }
}
