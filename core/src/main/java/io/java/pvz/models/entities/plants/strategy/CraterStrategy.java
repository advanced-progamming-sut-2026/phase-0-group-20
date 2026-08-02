package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.timeManager.TimeManager;

/**
 * Crater Strategy:
 * Used for Doom-shroom. Upon exploding (or dying), it leaves a crater
 * on its tile, making it unplantable for a certain duration.
 */

public class CraterStrategy implements IPlantStrategy {
    private boolean craterCreated = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if ((context.getCurrentHp() <= 0 || context.isDead()) && !craterCreated) {
            notify("🕳️ " + context.getName() + " exploded and left a deep crater on the tile!");
            if (context.getPlacedTile() != null) {
                Tile tile = context.getPlacedTile();
                int craterDurationTicks = (int) (context.getAbilityValue() * TimeManager.TICKS_PER_SECOND);
                tile.setCrater(true);
                tile.setCraterTimer(craterDurationTicks);
            }
            craterCreated = true;
        }
    }
}
