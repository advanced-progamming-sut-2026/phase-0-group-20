package models.game.adventure.levels.conditions;

import models.game.GameSession;
import models.game.WinCondition;

public class TimedWarWinCondition implements WinCondition {
    @Override
    public boolean isWon(GameSession session) {
        return false;
    }
}
