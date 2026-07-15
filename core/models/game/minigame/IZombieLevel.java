package models.game.minigame;

import models.enums.GameState;
import models.game.GameMode;
import models.game.GameSession;

public class IZombieLevel implements GameMode {
    @Override
    public void onStart(GameSession session) {

    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {

    }

    @Override
    public GameState checkResult(GameSession session) {
        return null;
    }
}
