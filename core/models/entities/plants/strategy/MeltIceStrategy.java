package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.fields.obstacle.IceHolder;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;

/**
 * Melt Ice Strategy:
 * Used for Hot Potato. Instantly melts ice blocks on the tile it is planted on,
 * freeing the frozen plant underneath, and then destroys itself.
 */

public class MeltIceStrategy implements IPlantStrategy {
    private static final int MELT_DELAY = (int) (0.5 * TimeManager.TICKS_PER_SECOND);
    private int startTick = -1;
    private boolean is3x3 = false;

    private boolean explodeOnFinish = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) startTick = currentTick;

        if (currentTick - startTick >= MELT_DELAY) {
            Tile currentTile = context.getPlacedTile();

            if (currentTile instanceof IceHolder && ((IceHolder) currentTile).isBlockedByIce()) {
                notify("🔥 Hot Potato melted the ice on its tile!");
                // change type of tile
            } else {
                notify("🔥 Hot Potato was planted, but there was no ice to melt!");
            }

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
    }

    public void setAreaOfEffect3x3(boolean areaOfEffect3x3) {
        this.is3x3 = areaOfEffect3x3;
    }

    public void setExplodeOnFinish(boolean explode) {
        this.explodeOnFinish = explode;
    }
}
