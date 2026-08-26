package io.java.pvz.models.game.events;

import io.java.pvz.models.timeManager.Ticker;
import io.java.pvz.models.timeManager.TimeManager;

public class DelayedEventTicker implements Ticker {

    private float remainingTicks;
    private final GameEvent event;
    private boolean finished = false;

    public DelayedEventTicker(GameEvent event, float delaySeconds) {
        this.event = event;
        this.remainingTicks = delaySeconds * TimeManager.TICKS_PER_SECOND;
    }

    @Override
    public void onTick(int currentTick) {
        if (finished) return;

        if (currentTick % remainingTicks == 0) {
            GameEventMessenger.getInstance().dispatch(event,
                new GameEventPayload.Builder(event).build());
            finished = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
