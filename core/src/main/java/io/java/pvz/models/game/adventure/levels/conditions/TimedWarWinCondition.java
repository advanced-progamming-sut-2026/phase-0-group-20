package io.java.pvz.models.game.adventure.levels.conditions;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.WinCondition;

public class TimedWarWinCondition implements WinCondition {
    @Override
    public boolean isWon(GameSession session) {
        return false;
    }
}
