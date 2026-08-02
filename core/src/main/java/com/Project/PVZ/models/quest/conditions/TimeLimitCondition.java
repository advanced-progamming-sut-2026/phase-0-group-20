package com.Project.PVZ.models.quest.conditions;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventPayload;
import com.Project.PVZ.models.timeManager.TimeManager;

public class TimeLimitCondition extends QuestCondition {

    @JsonProperty("timeLimitSeconds")
    private int timeLimitSeconds;

    @JsonProperty("waveStartTick")
    private int waveStartTick = -1; // Use -1 to clearly indicate it hasn't started

    public TimeLimitCondition(int timeLimitSeconds, int target) {
        this.timeLimitSeconds = timeLimitSeconds;
        this.targetProgress = target;
    }

    public TimeLimitCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();

        // 1. Record the game's exact internal tick when the wave starts
        if (event == GameEvent.WAVE_STARTED && waveStartTick == -1) {
            waveStartTick = GameSession.getInstance().getTimeManager().getCurrentTick();
        }

        // 2. Calculate the elapsed time using game ticks, not the real-world clock
        if (event == GameEvent.ZOMBIE_KILLED && waveStartTick != -1) {
            int currentTick = GameSession.getInstance().getTimeManager().getCurrentTick();
            int elapsedTicks = currentTick - waveStartTick;

            // Convert ticks to seconds using your physics constant
            int elapsedSeconds = elapsedTicks / TimeManager.TICKS_PER_SECOND;

            if (elapsedSeconds <= timeLimitSeconds) {
                currentProgress++;
            }
        }
    }
}
