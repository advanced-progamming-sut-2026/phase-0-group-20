package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.fields.tiles.GraveStoneTile;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;

/**
 * Grave Buster Strategy:
 * This plant can only be planted on a tile containing a Grave.
 * It takes a few seconds to consume the grave, destroying both the grave and itself.
 */

public class GraveBusterStrategy implements IPlantStrategy {
    private float eatTimeReduction = 0;
    private int startTick = -1;

    private boolean explodeOnFinish = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) startTick = currentTick;

        Tile currentTile = context.getPlacedTile();

        if (!(currentTile instanceof GraveStoneTile)) {
            notify("❌ Grave Buster must be planted on a GraveStone!");
            context.takeDamage(context.getCurrentHp());
            return;
        }

        float finalDelaySeconds = Math.max(0, 4.0f - eatTimeReduction);
        int bustDelayTicks = (int) (finalDelaySeconds * TimeManager.TICKS_PER_SECOND);

        if (currentTick - startTick >= bustDelayTicks) {
            notify("🪦 Grave Buster successfully destroyed the grave!");
            // change type of tile

            if (explodeOnFinish) {
                triggerExplosion(context);
            }
            context.takeDamage(context.getCurrentHp());
        }
    }

    private void triggerExplosion(Plant context) {
        int plantRow = context.getPlacedTile().getRow();
        int plantCol = context.getPlacedTile().getCol();
        int damage = 1800;

        notify("💥 " + context.getName() + " triggered a post-work explosion!");

        List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 1.5f);
        for (Zombie z : targets) {
            if (!z.isDead()) {
                z.takeDamage(damage);
                if (z.isDead()) {
                    context.onZombieDeath(z);
                }
            }
        }
        // this method for explod
    }

    public void reduceEatTime(float seconds) {
        this.eatTimeReduction += seconds;
    }

    public void setExplodeOnFinish(boolean explode) {
        this.explodeOnFinish = explode;
    }
}
