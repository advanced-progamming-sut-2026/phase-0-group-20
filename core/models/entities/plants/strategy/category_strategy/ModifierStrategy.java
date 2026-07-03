package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;

/**
 * Modifier Strategy:
 * Applies buffs or changes to allied entities (like projectiles) that pass through its tile.
 * For example, Torchwood ignites normal peas, doubling their damage.
 */

public class ModifierStrategy implements IPlantStrategy {

    @Override
    public void execute(Plant context, int currentTick) {
        // Typically, this logic checks if a friendly projectile is currently on this tile.
        // If yes, it transforms the projectile (e.g., changes Pea to Fire Pea).
    }
}
