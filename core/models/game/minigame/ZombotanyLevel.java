package models.game.minigame;

import models.entities.zombies.Wave;
import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;
import models.game.adventure.levels.conditions.NormalLoseCondition;
import models.game.adventure.levels.conditions.NormalWinCondition;

public class ZombotanyLevel extends Level {

    protected ZombotanyLevel(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int levelNumber) {
        super(name, season, waveCount, baseWaveDifficulty, levelNumber);
        this.addLoseCondition(new NormalLoseCondition());
        this.addWinCondition(new NormalWinCondition());
    }

    @Override
    public void onStart(GameSession session) {
        notify("Zombotany Level " + levelNumber + " Started!");


    }

    @Override
    protected void spawnWave(Wave wave, GameSession session) {



    }
}