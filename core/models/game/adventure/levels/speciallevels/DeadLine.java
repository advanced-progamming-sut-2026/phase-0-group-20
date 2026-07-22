package models.game.adventure.levels.speciallevels;

import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.SpecialLevel;
import models.game.adventure.levels.conditions.DeadLineLoseCondition;
import models.game.adventure.levels.conditions.NormalWinCondition;

public class DeadLine extends SpecialLevel {
    private final int LOSE_COL = 3;// zero_based

    public DeadLine(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        this.addWinCondition(new NormalWinCondition());
        this.addLoseCondition(new DeadLineLoseCondition(LOSE_COL));
    }

    @Override
    public void onStart(GameSession session) {
        notify("The dead line has been set to the" + (LOSE_COL + 1) + ".");
    }
}
