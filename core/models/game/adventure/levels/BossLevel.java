package models.game.adventure.levels;

import models.game.GameSession;
import models.game.adventure.SeasonType;

public class BossLevel extends Level {

    protected BossLevel(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int levelNumber) {
        super(name, season, waveCount, baseWaveDifficulty, levelNumber);
    }

    @Override
    public void onStart(GameSession session) {

    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {

    }
}
