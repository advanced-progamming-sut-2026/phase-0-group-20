package models.quest.conditions;

import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class TimeLimitCondition extends QuestCondition {
    private int timeLimit;
    private int timer = 0;

    public TimeLimitCondition(int timeLimit, int target) {
        this.timeLimit = timeLimit;
        this.targetProgress = target;
    }
    public TimeLimitCondition(){}

    @Override
    public void updateProgress(GameEventPayload payload) {
        timer++;
        if (timer <= timeLimit && payload.getType() == GameEvent.ZOMBIE_KILLED) {
            currentProgress++;
        }
    }
}
