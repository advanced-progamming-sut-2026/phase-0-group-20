package models.game.adventure.levels.speciallevels;

import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.SpecialLevel;

public class LockedPlants extends SpecialLevel {

    protected LockedPlants(String name, SeasonType season, int waveCount, int baseWaveDifficulty) {
        super(name, season, waveCount, baseWaveDifficulty);
    }

    @Override
    public void onStart(GameSession session) {

    }
}
