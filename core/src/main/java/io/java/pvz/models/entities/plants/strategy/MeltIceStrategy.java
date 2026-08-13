package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.obstacle.IceHolder;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;

/**
 * Melt Ice Strategy:
 * Used for Hot Potato. Instantly melts ice blocks on the tile it is planted on,
 * freeing the frozen plant underneath, and then destroys itself.
 */

public class MeltIceStrategy implements IPlantStrategy {
    private static final int MELT_DELAY = (int) (0.5 * TimeManager.TICKS_PER_SECOND);
    private int startTick = -1;

    private boolean explodeOnFinish = false;
    private boolean meltArea3x3 = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) startTick = currentTick;

        if (currentTick - startTick >= MELT_DELAY) {
            Tile currentTile = context.getPlacedTile();
            int centerRow = currentTile.getRow();
            int centerCol = currentTile.getCol();

            boolean meltedAnything = false;

            if (meltArea3x3) {
                for (int r = centerRow - 1; r <= centerRow + 1; r++) {
                    for (int c = centerCol - 1; c <= centerCol + 1; c++) {
                        if (meltIceOnTile(r, c)) meltedAnything = true;
                    }
                }
            } else {
                if (meltIceOnTile(centerRow, centerCol)) meltedAnything = true;
            }

            if (meltedAnything) {
                GameSession.notify("🔥 " + context.getName() + " melted the ice!");
            } else {
                GameSession.notify("🔥 " + context.getName() + " was planted, but there was no ice to melt!");
            }

            if (explodeOnFinish) {
                triggerExplosion(context);
            }
            context.takeDamage(context.getCurrentHp());
        }
    }

    private boolean meltIceOnTile(int row, int col) {
        Tile tile = GameSession.getInstance().getArena().getTile(row, col);
        if (tile == null) return false;

        boolean melted = false;

        if (tile instanceof IceHolder iceHolder) {
            if (iceHolder.hasIceBlock()) {
                iceHolder.removeIceBlock();
                melted = true;
            }
        }

        for (Plant p : tile.getPlants()) {
            if (p.isFrozen()) {
                p.damageIceBlock(99999);
                melted = true;
            }
        }
        return melted;
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


    public void setExplodeOnFinish(boolean explode) {
        this.explodeOnFinish = explode;
    }

    public void setMeltArea3x3(boolean meltArea3x3) {
        this.meltArea3x3 = meltArea3x3;
    }
}
