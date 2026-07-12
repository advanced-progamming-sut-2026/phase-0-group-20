package models.quest.conditions;

import models.game.GameEvent;
import models.game.GameEventPayload;

public class LawnMoverKillsCondition extends QuestCondition {
    public LawnMoverKillsCondition(int amount) {
        targetProgress = amount;
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event != GameEvent.ZOMBIE_KILLED_LAWN_MOWER) return;
        currentProgress++;


    }
}
