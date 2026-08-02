package io.java.pvz.models.game.minigame.minigameCondition;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.WinCondition;

public class IZombieWinCondition implements WinCondition {

    @Override
    public boolean isWon(GameSession session) {
        for (int i = 0; i < session.getArena().getRows(); i++)
            if (session.getArena().getBrainInRow(i) != null && !session.getArena().getBrainInRow(i).isEaten())
                return false;
        return true;
    }
}
