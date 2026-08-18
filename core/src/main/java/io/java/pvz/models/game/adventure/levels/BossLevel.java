package io.java.pvz.models.game.adventure.levels;

import io.java.pvz.models.entities.zombies.zomboss.*;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.conditions.NormalLoseCondition;
import io.java.pvz.models.game.adventure.levels.conditions.BossWinCondition;
import io.java.pvz.models.game.adventure.levels.speciallevels.ConveyorBelt;
import io.java.pvz.models.timeManager.TimeManager;

public class BossLevel extends ConveyorBelt {

    private Zomboss zomboss;

    public BossLevel(String name, SeasonType season, int levelNumber) {
        super(name, season, 0, 0, levelNumber);

        this.winConditions.clear();
        this.loseConditions.clear();

        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onLevelStart(GameSession session) {
        super.onLevelStart(session);

        int middleRow = session.getArena().getRows() / 2 - 1;

        zomboss = switch (season) {
            case ANCIENT_EGYPT -> new SpiderZomboss(middleRow);
            case FROZEN_CAVES -> new MammothZomboss(middleRow);
            case DARK_AGES -> new DragonZomboss(middleRow);
            case BIG_WAVE_BEACH -> new SharkZomboss(middleRow);
            default -> new SpiderZomboss(middleRow);
        };

        session.getArena().addZombie(zomboss);
        session.getTimeManager().registerNewTicker(zomboss);

        this.addWinCondition(new BossWinCondition(zomboss));

        notify("Dr. Zomboss has arrived! Defeat him to win!");
    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {

        if (currentTick > 0 && currentTick % (6 * TimeManager.TICKS_PER_SECOND) == 0) {
            if (getBelt().size() < 10) {
                spawnPlantOnBelt();
            }
        }
    }

    @Override
    public float getDifficultyCoefficient() {
        return super.getDifficultyCoefficient() * 1.5f;
    }

    public Zomboss getZomboss() {
        return zomboss;
    }

    @Override
    public String toString() {
        return "Don't Let Zombies Eat Your Brain-Defeat The Zomboss";
    }
}
