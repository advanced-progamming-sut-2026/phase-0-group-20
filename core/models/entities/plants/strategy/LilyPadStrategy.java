package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.fields.tiles.Tile;
import models.game.GameSession;
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
                        GameSession.notify("💦 " + stackedPlant.getName()
                                + " drowned because its Lily Pad was destroyed!");
                        stackedPlant.takeDamage(stackedPlant.getCurrentHp());
                    }
                }
            }
        }
    }
}