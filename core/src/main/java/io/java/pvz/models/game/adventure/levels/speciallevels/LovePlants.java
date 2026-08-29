package io.java.pvz.models.game.adventure.levels.speciallevels;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.SpecialLevel;
import io.java.pvz.models.game.adventure.levels.conditions.LovePlantLoseCondition;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;

import java.util.Random;

public class LovePlants extends SpecialLevel {
    private final LovePlantLoseCondition loseCondition;
    private final int limit;

    public LovePlants(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        limit = new Random().nextInt(3) + 3;
        this.loseCondition = new LovePlantLoseCondition(limit);
        this.addLoseCondition(loseCondition);

    }

    @Override
    public void onLevelStart(GameSession session) {
        notify("Love Plants Started: do not lose " + limit + " or above number of plants or you lose.");
        GameEventMessenger.getInstance().addListener(GameEvent.PLANT_LOST, loseCondition);
    }

    @Override
    public void destroyLevelFields() {
        GameEventMessenger.getInstance().removeListener(GameEvent.PLANT_LOST, loseCondition);
    }

    @Override
    public String toString() {
        return "Don't Let Zombies Eat Your Brain-Don't Lose Over " + limit + " Plants.";
    }
}
