package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.obstacle.IceHolder;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.AnimationCatalog;

import java.util.List;

public class MeltIceStrategy implements IPlantStrategy {
    private int startTick = -1;
    private int meltDelayTicks = 0;

    private boolean explodeOnFinish = false;
    private boolean meltArea3x3 = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (startTick == -1) {
            startTick = currentTick;
            context.triggerAction("attack");

            float animDuration = AnimationCatalog.getPlantAnimation(context).getDuration("attack");
            meltDelayTicks = (int) (animDuration * TimeManager.TICKS_PER_SECOND);

            if (meltDelayTicks <= 0) meltDelayTicks = (int) (1.0f * TimeManager.TICKS_PER_SECOND);
        }

        if (currentTick - startTick >= meltDelayTicks) {
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


            if (explodeOnFinish) {
                triggerExplosion(context);
            }

            context.takeDamage(context.getCurrentHp());
        }
    }

    private boolean meltIceOnTile(int row, int col) {
        Arena arena = GameSession.getInstance().getArena();

        if (row < 0 || row >= arena.getRows() || col < 0 || col >= arena.getCols()) {
            return false;
        }

        Tile tile = arena.getTile(row, col);
        if (tile == null) return false;

        boolean melted = false;

        if (tile instanceof IceHolder iceHolder) {
            if (iceHolder.hasIceBlock()) {
                iceHolder.takeIceDamage(99999);
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
