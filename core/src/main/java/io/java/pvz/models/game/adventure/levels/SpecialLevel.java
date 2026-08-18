package io.java.pvz.models.game.adventure.levels;

import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.conditions.NormalLoseCondition;
import io.java.pvz.models.game.adventure.levels.conditions.NormalWinCondition;

public abstract class SpecialLevel extends Level {
    protected SpecialLevel(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        this.addLoseCondition(new NormalLoseCondition());
        this.addWinCondition(new NormalWinCondition());
    }
    @Override
    public String toString() {
        return "Don't Let Zombies Eat Your Brain";
    }
}
