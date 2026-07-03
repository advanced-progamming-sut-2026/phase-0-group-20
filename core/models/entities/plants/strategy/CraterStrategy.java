package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Crater Strategy:
 * Used for Doom-shroom. Upon exploding (or dying), it leaves a crater
 * on its tile, making it unplantable for a certain duration.
 */

public class CraterStrategy implements IPlantStrategy {
    private boolean craterCreated = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (context.getCurrentHp() <= 0 && !craterCreated) {
            System.out.println(context.getName() + " left a deep crater on the tile!");

            if (context.getPlacedTile() != null) {
                // Logic to mark the tile as a crater
            }

            craterCreated = true;
        }
    }
}
