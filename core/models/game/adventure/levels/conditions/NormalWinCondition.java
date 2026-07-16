package models.game.adventure.levels.conditions;

import models.game.GameSession;
import models.game.WinCondition;
import models.game.adventure.levels.Level;

public class NormalWinCondition implements WinCondition {

    @Override
    public boolean isWon(GameSession session) {
        if (!(session.getCurrentMode() instanceof Level currentLevel)) {
            return false;
        }

        if (!currentLevel.allWavesSpawned()) {
            return false;
        }

        if (!session.getArena().getActiveZombies().isEmpty()) {
            return false;
        }
        return true;
    }
}
