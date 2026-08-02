package io.java.pvz.models.game.adventure.levels;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.conditions.NormalLoseCondition;
import io.java.pvz.models.game.adventure.levels.conditions.NormalWinCondition;

public class NormalLevel extends Level {

    protected NormalLevel(String name, SeasonType season, int waveCount, int baseWaveBudget, int levelNumber) {
        super(name, season, waveCount, baseWaveBudget, levelNumber);
        this.addWinCondition(new NormalWinCondition());
        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onLevelStart(GameSession session) {
        // Empty for now
    }


}
