package models.game.minigame.minigameCondition;

import models.game.GameSession;

public class BowlingCondition extends MiniGameWinCondition {

    @Override
    public boolean isWon(GameSession session) {
        return false;
    }
}
