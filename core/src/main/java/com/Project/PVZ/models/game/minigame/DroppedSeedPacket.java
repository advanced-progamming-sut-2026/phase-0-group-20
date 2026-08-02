package com.Project.PVZ.models.game.minigame;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventMessenger;
import com.Project.PVZ.models.game.events.GameEventPayload;
import com.Project.PVZ.models.timeManager.Ticker;

public class DroppedSeedPacket implements Ticker {

    private static final int TIMER = 100;

    private final Plant plant;
    private final int row;
    private final int col;
    private int timeLeft = TIMER;
    private boolean isExpired = false;

    public DroppedSeedPacket(Plant plant, int row, int col) {
        this.plant = plant;
        this.row = row;
        this.col = col;
    }

    @Override
    public void onTick(int currentTick) {
        if (isExpired) return;
        timeLeft--;
        if (timeLeft <= 0) {
            isExpired = true;
            GameSession.getInstance().getArena().getDroppedSeedPackets().remove(this);
            GameSession.getInstance().getTimeManager().unregisterTicker(this);
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                            .message("A " + plant.getName() + " seed packet vanished!")
                            .build());
        }
    }

    public Plant getPlant() {
        return plant;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public boolean isExpired() {
        return isExpired;
    }

    public void setExpired(boolean expired) {
        isExpired = expired;
    }
}
