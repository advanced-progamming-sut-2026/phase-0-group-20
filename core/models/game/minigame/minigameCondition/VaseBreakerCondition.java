package models.game.minigame.minigameCondition;

import models.game.GameSession;

public class VaseBreakerCondition extends MiniGameWinCondition {
    @Override
    public boolean isWon(GameSession session) {
        return false;
    }
}
