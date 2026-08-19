package io.java.pvz.models.fields;

import io.java.pvz.models.Position;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.Ticker;
import io.java.pvz.models.timeManager.TimeManager;

public class LawnMower implements Ticker {
    private static final float HORIZONTAL_SPEED = 300f / TimeManager.TICKS_PER_SECOND;
    ;
    private final int row;
    private final Arena arena;
    private boolean activate;
    private boolean isDestroyed = false;
    private final Position position;

    public LawnMower(int row, Arena arena) {
        this.row = row;
        this.arena = arena;
        this.activate = false;
        this.position = new Position(-1, row);
    }

    @Override
    public void onTick(int currentTick) {
        if (isDestroyed) return;

        if (this.activate) {
            position.moveX(HORIZONTAL_SPEED);

            if (position.getCol() >= arena.getCols()) {
                this.isDestroyed = true;
            }
        }
    }

    public void trigger() {
        this.activate = true;
        GameEventMessenger.getInstance().dispatch(GameEvent.LAWNMOWER_TRIGGERED, new GameEventPayload
            .Builder(GameEvent.LAWNMOWER_TRIGGERED).
            build());
    }

    public Position getPosition() {
        return position;
    }

    public boolean isDestroyed() {
        return isDestroyed;
    }

    public boolean isActivate() {
        return activate;
    }

    public int getRow() {
        return row;
    }
}
