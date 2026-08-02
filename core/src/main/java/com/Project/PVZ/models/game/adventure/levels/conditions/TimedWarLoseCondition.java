package com.Project.PVZ.models.game.adventure.levels.conditions;

import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.LoseCondition;

public class TimedWarLoseCondition implements LoseCondition {

    @Override
    public boolean isLost(GameSession session) {
        return false;
    }
}
