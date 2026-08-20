package io.java.pvz.models.game.minigame.minigameCondition;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.LoseCondition;
import io.java.pvz.models.timeManager.TimeManager;

public class IZombieTimeLimitLoseCondition implements LoseCondition {

    public static final int DEFAULT_TIME_LIMIT_SECONDS = 180;

    private final int timeLimitTicks;

    public IZombieTimeLimitLoseCondition() {
        this(DEFAULT_TIME_LIMIT_SECONDS);
    }

    public IZombieTimeLimitLoseCondition(int timeLimitSeconds) {
        this.timeLimitTicks = timeLimitSeconds * TimeManager.TICKS_PER_SECOND;
    }

    @Override
    public boolean isLost(GameSession session) {
        if (session.getTimeManager().getCurrentTick() < timeLimitTicks) return false;

        for (int i = 0; i < session.getArena().getRows(); i++) {
            if (session.getArena().getBrainInRow(i) != null && !session.getArena().getBrainInRow(i).isEaten()) {
                return true;
            }
        }
        return false;
    }

    public int getTimeLimitTicks() {
        return timeLimitTicks;
    }
}
