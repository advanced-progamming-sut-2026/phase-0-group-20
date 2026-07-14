package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.fields.obstacle.IceHolder;
import models.fields.tiles.Tile;
import models.timeManager.TimeManager;

/**
 * Melt Ice Strategy:
 * Used for Hot Potato. Instantly melts ice blocks on the tile it is planted on,
 * freeing the frozen plant underneath, and then destroys itself.
 */

public class MeltIceStrategy implements IPlantStrategy {
    private static final int MELT_DELAY = (int) (0.5 * TimeManager.TICKS_PER_SECOND);
    private int startTick = -1;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) startTick = currentTick;

        if (currentTick - startTick >= MELT_DELAY) {
            Tile currentTile = context.getPlacedTile();

            if (currentTile instanceof IceHolder && ((IceHolder) currentTile).isBlockedByIce()) {
                System.out.println("🔥 Hot Potato melted the ice on its tile!");
                // change type of tile
            } else {
                System.out.println("🔥 Hot Potato was planted, but there was no ice to melt!");
            }

            context.takeDamage(context.getCurrentHp());
        }
    }
}
