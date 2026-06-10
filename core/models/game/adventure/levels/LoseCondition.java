package models.game.adventure.levels;

import models.enums.GameState;

public interface LoseCondition {
    boolean isLost(GameState state);
}
