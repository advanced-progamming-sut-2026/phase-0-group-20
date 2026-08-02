package io.java.pvz.models.game.adventure.levels.speciallevels;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.SpecialLevel;

public class PlantWhatYouGet extends SpecialLevel {

    protected PlantWhatYouGet(
            String name
            , SeasonType season
            , int waveCount
            , int baseWaveBudget
            , int globalLevelNumber
    ) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
    }

    @Override
    public void onLevelStart(GameSession session) {

    }
}
