package com.Project.PVZ.models.game.adventure.levels.conditions;

import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.WinCondition;
import com.Project.PVZ.models.game.adventure.levels.Level;

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
