package com.Project.PVZ.models.game.adventure.levels.conditions;

import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.WinCondition;

public class TimedWarWinCondition implements WinCondition {
    @Override
    public boolean isWon(GameSession session) {
        return false;
    }
}
