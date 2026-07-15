package models.game;

import models.enums.GameState;

public interface GameMode {
    void onStart(GameSession session);

    void dorosteshKonin(GameSession session, int currentTick); // it's different from the onTick in time manager

    GameState checkResult(GameSession session);
}