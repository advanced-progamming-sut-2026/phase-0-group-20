package models.game.minigame.minigameCondition;

import models.game.GameSession;
import models.game.WinCondition;

public class VaseBreakerCondition implements WinCondition {

    @Override
    public boolean isWon(GameSession session) {
        return false;
    }

}
