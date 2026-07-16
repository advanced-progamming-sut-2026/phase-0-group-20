package models.quest.conditions;

import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class SunCollectCondition extends QuestCondition {
    public SunCollectCondition(int amount) {
        this.targetProgress = amount;
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        if (payload.getType() != GameEvent.SUN_COLLECTED)
            return;
        currentProgress+=payload.getAmount();
    }
}
