package com.Project.PVZ.models.quest.conditions;

import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventPayload;

public class SunCollectCondition extends QuestCondition {
    public SunCollectCondition(int amount) {
        this.targetProgress = amount;
    }

    public SunCollectCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        if (payload.getType() != GameEvent.SUN_COLLECTED)
            return;
        currentProgress += payload.getAmount();
    }
}
