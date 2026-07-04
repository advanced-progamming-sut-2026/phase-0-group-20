package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.game.GameSession;

/**
 * Anti-Jump Strategy:
 * A passive defense strategy. When attached to a tall defensive plant (like Tall-nut),
 * it acts as a signal to vaulting/jumping zombies (e.g., Pole Vaulting Zombie)
 * that they cannot jump over this tile and must stop to eat it.
 */

public class AntiJumpStrategy implements IPlantStrategy {
    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        // This is a passive marker.
        // The jumping zombie's logic will check if the plant has this strategy
        // before attempting to jump over the tile.
    }
}
