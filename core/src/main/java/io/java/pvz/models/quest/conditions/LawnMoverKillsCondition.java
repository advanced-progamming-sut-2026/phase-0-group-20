package io.java.pvz.models.quest.conditions;

import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventPayload;

public class LawnMoverKillsCondition extends QuestCondition {
    public LawnMoverKillsCondition(int amount) {
        targetProgress = amount;
    }

    public LawnMoverKillsCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event != GameEvent.ZOMBIE_KILLED_LAWN_MOWER) return;
        currentProgress++;


    }
}
