package models.game.adventure.levels;

import models.game.GameSession;
import models.game.adventure.SeasonType;

public class BossLevel extends Level {

    protected BossLevel(String name, SeasonType season, int waveCount, int baseWaveBudget, int levelNumber) {
        super(name, season, waveCount, baseWaveBudget, levelNumber);
    }


    @Override
    public void onLevelStart(GameSession session) {

    }


    @Override
    public float getDifficultyCoefficient() {
        return super.getDifficultyCoefficient() * 1.5f;
    }
}
