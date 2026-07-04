package models.game.minigame.minigameCondition;

import models.game.GameSession;

public class IZombieCondition extends MiniGameWinCondition {

    @Override
    public boolean isWon(GameSession session) {
        return false;
    }
}
