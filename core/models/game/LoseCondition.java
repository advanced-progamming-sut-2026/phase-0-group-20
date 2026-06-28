package models.game;

import models.enums.GameState;

public interface LoseCondition {
    boolean isLost(Game state);
}
