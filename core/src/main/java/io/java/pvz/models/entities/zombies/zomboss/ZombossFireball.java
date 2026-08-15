package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieFactory;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.Ticker;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public class ZombossFireball implements Ticker {
    private float currentX, currentY;
    private final float startX, startY;
    private final float targetX, targetY;
    private final int targetCol, targetRow;

    private int flightTicks;
    private final int totalFlightTicks = 2 * TimeManager.TICKS_PER_SECOND;
    private boolean isDestroyed = false;

    public ZombossFireball(float startX, float startY, float targetX, float targetY, int targetCol, int targetRow) {
        this.currentX = startX;
        this.currentY = startY;
        this.startX = startX;
        this.startY = startY;
        this.targetX = targetX;
        this.targetY = targetY;
        this.targetCol = targetCol;
        this.targetRow = targetRow;
        this.flightTicks = 0;
    }

    @Override
    public void onTick(int currentTick) {
        if (isDestroyed) return;

        flightTicks++;
        float progress = (float) flightTicks / totalFlightTicks;

        if (progress >= 1.0f) {
            impact();
            return;
        }

        if (progress < 0.5f) {
            float localProgress = progress * 2f;
            currentX = startX;
            currentY = startY + (800f * localProgress);
        } else {
            float localProgress = (progress - 0.5f) * 2f;
            currentX = targetX;
            currentY = (targetY + 800f) - (800f * localProgress);
        }
    }

    private void impact() {
        this.isDestroyed = true;
        GameSession session = GameSession.getInstance();
        Tile targetTile = session.getArena().getTile(targetRow, targetCol);

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("FIREBALL_EXPLOSION")
                .pixelCoordinate(targetX, targetY)
                .build());

        DragonScorchedEarthAttack.burnTheTile(targetTile);

        Zombie impDragon = ZombieFactory.create(ZombieType.IMP, targetRow);
        impDragon.setCol(targetCol);
        impDragon.setX(targetX);
        session.getArena().addZombie(impDragon);
        session.getTimeManager().registerNewTicker(impDragon);

        session.getTimeManager().unregisterTicker(this);
    }

    public float getX() { return currentX; }
    public float getY() { return currentY; }
    public boolean isDestroyed() { return isDestroyed; }
}
