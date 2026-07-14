package models.entities.plants.strategy;

import models.entities.plants.Plant;

/**
 * Lily Pad Strategy:
 * Passive marker. Allows a non-aquatic plant to be planted on top of this tile
 * even though the tile itself is water. Also acts as the "stack" anchor:
 * the plant placed on top effectively replaces the Lily Pad's slot.
 */
public class LilyPadStrategy implements IPlantStrategy {
    @Override
    public void execute(Plant context, int currentTick) {
        // Passive marker - no per-tick behavior.
    }
}