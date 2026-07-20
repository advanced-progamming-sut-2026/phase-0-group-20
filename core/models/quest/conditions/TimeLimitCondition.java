package models.quest.conditions;

import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class TimeLimitCondition extends QuestCondition {
    private int timeLimitSeconds;
    private long waveStartTime = 0;

    public TimeLimitCondition(int timeLimitSeconds, int target) {
        this.timeLimitSeconds = timeLimitSeconds;
        this.targetProgress = target;
    }
    public TimeLimitCondition(){}

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();

        if (event == GameEvent.WAVE_STARTED && waveStartTime == 0) {
            waveStartTime = System.currentTimeMillis();
        }

        if (event == GameEvent.ZOMBIE_KILLED && waveStartTime > 0) {
            long elapsedTime = (System.currentTimeMillis() - waveStartTime) / 1000;
            if (elapsedTime <= timeLimitSeconds) {
                currentProgress++;
            }
        }
    }
}