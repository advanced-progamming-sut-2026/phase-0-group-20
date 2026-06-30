package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Melt Ice Strategy:
 * Used for Hot Potato. Instantly melts ice blocks on the tile it is planted on,
 * freeing the frozen plant underneath, and then destroys itself.
 */

public class MeltIceStrategy implements IPlantStrategy {
    private boolean hasMelted = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (!hasMelted) {
            System.out.println(context.getName() + " instantly melted the ice block!");

            if (context.getPlacedTile() != null) {
                // Logic to remove the ice status from the tile/plant
            }

            hasMelted = true;

            // The Hot Potato is consumed instantly after doing its job
            context.takeDamage(context.getCurrentHp());
        }
    }
}
