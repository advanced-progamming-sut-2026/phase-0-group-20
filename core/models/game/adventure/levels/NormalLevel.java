package models.game.adventure.levels;

import models.game.GameSession;
import models.game.adventure.SeasonType;

public class NormalLevel extends Level {

    protected NormalLevel(String name, SeasonType season, int waveCount, int baseWaveDifficulty) {
        super(name, season, waveCount, baseWaveDifficulty);
    }

    @Override
    public void onStart(GameSession session) {

    }
}
