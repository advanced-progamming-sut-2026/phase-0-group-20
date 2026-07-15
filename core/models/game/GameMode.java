package models.game;

import models.enums.GameState;

public interface GameMode {
    void onStart(GameSession session);

    void engineLoop(GameSession session, int currentTick); // it's different from the dorosteshKonin in time manager

    GameState checkResult(GameSession session);
}