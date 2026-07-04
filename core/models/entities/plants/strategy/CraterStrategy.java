package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.game.GameSession;

/**
 * Crater Strategy:
 * Used for Doom-shroom. Upon exploding (or dying), it leaves a crater
 * on its tile, making it unplantable for a certain duration.
 */

public class CraterStrategy implements IPlantStrategy {
    private boolean craterCreated = false;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        if (context.getCurrentHp() <= 0 && !craterCreated) {
            System.out.println("🕳️ " + context.getName() + " exploded and left a deep crater on the tile!");

            if (context.getPlacedTile() != null) {
                int row = context.getPlacedTile().getRow();
                int col = context.getPlacedTile().getCol();

                // change tile
            }

            craterCreated = true;
        }
    }
}
