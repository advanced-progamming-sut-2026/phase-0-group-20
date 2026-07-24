package models.game.adventure.levels;

import models.App;
import models.entities.plants.Plant;
import models.enums.plants.PlantCategory;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.speciallevels.ConveyorBelt;
import models.game.adventure.levels.speciallevels.DeadLine;
import models.game.adventure.levels.speciallevels.LockedPlants;
import models.game.adventure.levels.speciallevels.LovePlants;

import java.util.*;

public class LevelFactory {

    private static final int BOSS_WAVE_ADDITION = 2;
    public static Level createLevel(SeasonType season, int chapterLevelIndex) { //the index of level in each chapter
        int waveCount = 2 + (chapterLevelIndex / 2);
        int baseWaveBudget = 1000 + (chapterLevelIndex * 200);

        return switch (chapterLevelIndex) {
            case 0, 1 -> new NormalLevel("Level " + (chapterLevelIndex + 1),
                    season, waveCount, baseWaveBudget, chapterLevelIndex + 1);
            case 2 -> createSpecialLevel(season, (chapterLevelIndex + 1), waveCount, baseWaveBudget);
            case 3 -> new BossLevel("Boss Level" + season.toString().toLowerCase(), season,
                    waveCount + BOSS_WAVE_ADDITION, baseWaveBudget * BOSS_WAVE_ADDITION, (chapterLevelIndex + 1));
            default -> throw new IllegalArgumentException("Invalid chapter level index");
        };
    }

    private static SpecialLevel createSpecialLevel
            (SeasonType season, int globalLevelNumber, int waveCount, int baseWaveBudget) {
        return switch (season) {
            case ANCIENT_EGYPT ->
                    new ConveyorBelt("Egypt Conveyor", season, waveCount, baseWaveBudget, globalLevelNumber);

            case FROZEN_CAVES ->
                    new DeadLine("Ice Caves Deadline", season, waveCount, baseWaveBudget, globalLevelNumber);

            case BIG_WAVE_BEACH ->
                    new LovePlants("Beach Don't Lose Plants", season, waveCount, baseWaveBudget, globalLevelNumber);

            default -> new LockedPlants("Dark Ages Locked", season, waveCount, baseWaveBudget, globalLevelNumber,
                    new Random().nextInt(3)+1);
        };
    }


}