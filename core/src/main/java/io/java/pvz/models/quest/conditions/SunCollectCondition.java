package io.java.pvz.models.quest.conditions;

import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventPayload;

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
