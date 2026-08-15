package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.Ticker;
import io.java.pvz.models.timeManager.TimeManager;

public class ZombossMissile implements Ticker {
    private float currentX, currentY;
    private final float targetX, targetY;
    private final float startX, startY;
    private final int targetRow, targetCol;

    private final MissileImpactBehavior impactBehavior;

    private int ticksElapsed = 0;

    private static final int DELAY_TICKS  = (int) (0.5 * TimeManager.TICKS_PER_SECOND);
    private static final int FLIGHT_TICKS = (int) (1.5 * TimeManager.TICKS_PER_SECOND);
    private boolean isDestroyed = false;

    public ZombossMissile(float targetX, float targetY, int targetRow, int targetCol,
                          MissileImpactBehavior impactBehavior) {
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetRow = targetRow;
        this.targetCol = targetCol;
        this.impactBehavior = impactBehavior;

        this.startX = targetX;
        this.startY = targetY + 900f;

        this.currentX = startX;
        this.currentY = startY;
    }

    @Override
    public void onTick(int currentTick) {
        if (isDestroyed) return;

        ticksElapsed++;

        if (ticksElapsed <= DELAY_TICKS) {
            return;
        }

        int movingTicks = ticksElapsed - DELAY_TICKS;
        float progress = (float) movingTicks / FLIGHT_TICKS;

        if (progress >= 1.0f) {
            currentX = targetX;
            currentY = targetY;
            impact();
            return;
        }

        currentX = startX + (targetX - startX) * progress;
        currentY = startY + (targetY - startY) * progress;
    }

    private void impact() {
        this.isDestroyed = true;
        GameSession session = GameSession.getInstance();
        Tile targetTile = session.getArena().getTile(targetRow, targetCol);

        if (targetTile != null && impactBehavior != null) {
            impactBehavior.onImpact(targetTile);
        }

        session.getTimeManager().unregisterTicker(this);
    }

    public float getX() { return currentX; }
    public float getY() { return currentY; }
}
