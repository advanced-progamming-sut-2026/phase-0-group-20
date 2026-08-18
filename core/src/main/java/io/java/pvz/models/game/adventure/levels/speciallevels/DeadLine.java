package io.java.pvz.models.game.adventure.levels.speciallevels;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.RedLineCapable;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.SpecialLevel;
import io.java.pvz.models.game.adventure.levels.conditions.DeadLineLoseCondition;

public class DeadLine extends SpecialLevel implements RedLineCapable {
    private static final int LOSE_COL = 2;// zero_based

    public DeadLine(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        this.addLoseCondition(new DeadLineLoseCondition(LOSE_COL));
    }

    @Override
    public void onLevelStart(GameSession session) {
        notify("The dead line has been set to the" + (LOSE_COL + 1) + ".");
    }

    @Override
    public int getRedLineCol() {
        return LOSE_COL + 1;
    }

    @Override
    public String toString() {
        return "Don't Let Zombies Pass the Dead Line on Col "+(LOSE_COL + 1) + ".";
    }
}
