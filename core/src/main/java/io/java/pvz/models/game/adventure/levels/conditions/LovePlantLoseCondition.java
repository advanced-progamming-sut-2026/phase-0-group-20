package io.java.pvz.models.game.adventure.levels.conditions;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.LoseCondition;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventPayload;

public class LovePlantLoseCondition implements LoseCondition, GameEventListener {
    private final int limit;
    private int current;

    public LovePlantLoseCondition(int limit) {
        this.limit = limit;
        current = 0;
    }

    @Override
    public boolean isLost(GameSession session) {
        return current >= limit;
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.PLANT_LOST) {
            current++;
        }
    }
}
