package models.game.adventure.levels;

import models.enums.GameState;

public interface WinCondition {
    boolean isWon(GameState state);
}
