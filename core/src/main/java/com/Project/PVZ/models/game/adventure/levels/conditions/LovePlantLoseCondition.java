package com.Project.PVZ.models.game.adventure.levels.conditions;

import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.LoseCondition;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventListener;
import com.Project.PVZ.models.game.events.GameEventPayload;

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
