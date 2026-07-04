package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.fields.tiles.GraveStoneTile;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.timeManager.TimeManager;

/**
 * Grave Buster Strategy:
 * This plant can only be planted on a tile containing a Grave.
 * It takes a few seconds to consume the grave, destroying both the grave and itself.
 */

public class GraveBusterStrategy implements IPlantStrategy {
    private final int BUST_DELAY = 4 * TimeManager.TICKS_PER_SECOND;
    private int startTick = -1;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        if (startTick == -1) startTick = currentTick;

        Tile currentTile = context.getPlacedTile();

        if (!(currentTile instanceof GraveStoneTile)) {
            System.out.println("❌ Grave Buster must be planted on a GraveStone!");
            context.takeDamage(context.getCurrentHp());
            return;
        }

        if (currentTick - startTick >= BUST_DELAY) {
            System.out.println("🪦 Grave Buster successfully destroyed the grave!");
            // change type of tile
            context.takeDamage(context.getCurrentHp());
        }
    }
}
