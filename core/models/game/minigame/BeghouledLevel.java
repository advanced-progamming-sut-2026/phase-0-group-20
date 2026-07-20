package models.game.minigame;

import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;

public class BeghouledLevel extends Level {

    protected BeghouledLevel(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int levelNumber) {
        super(name, season, waveCount, baseWaveDifficulty, levelNumber);
    }

    @Override
    public void onStart(GameSession session) {

    }

}
