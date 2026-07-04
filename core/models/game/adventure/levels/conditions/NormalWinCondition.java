package models.game.adventure.levels.conditions;

import models.game.GameSession;
import models.game.WinCondition;
import models.game.adventure.levels.Level;

public class NormalWinCondition implements WinCondition {

    @Override
    public boolean isWon(GameSession session) {
        Level currentLevel = session.getCurrentChapter().getCurrentLevel();

        if (!currentLevel.allWavesSpawned()) {
            return false;
        }
    }
}
