package models.game.adventure.levels;

import models.game.adventure.SeasonType;
import models.game.adventure.levels.speciallevels.ConveyorBelt;
import models.game.adventure.levels.speciallevels.DeadLine;
import models.game.adventure.levels.speciallevels.LockedPlants;
import models.game.adventure.levels.speciallevels.LovePlants;

import java.util.ArrayList;

public class LevelFactory {

    public static Level createLevel(SeasonType season, int chapterLevelIndex) {
        int waveCount = 2 + (chapterLevelIndex / 2);
        int baseDifficulty = chapterLevelIndex * 5;

        return switch (chapterLevelIndex) {
            case 1, 2 ->
                    new NormalLevel("Level " + (chapterLevelIndex + 1), season, waveCount, baseDifficulty, chapterLevelIndex + 1);
            case 3 -> createSpecialLevel(season, (chapterLevelIndex + 1), waveCount, baseDifficulty);
            case 4 -> new BossLevel("Boss Level" + season.toString().toLowerCase(), season,
                    waveCount + 2, baseDifficulty * 2, (chapterLevelIndex + 1));
            default -> throw new IllegalArgumentException("Invalid chapter level index");
        };
    }

    private static SpecialLevel createSpecialLevel(SeasonType season, int globalLevelNumber, int waveCount, int baseDifficulty) {
        return switch (season) {
            case ANCIENT_EGYPT -> new ConveyorBelt("Egypt Conveyor", season, waveCount, baseDifficulty);

            case FROZEN_CAVES -> new DeadLine("Ice Caves Deadline", season, waveCount, baseDifficulty, 3);

            case BIG_WAVE_BEACH -> new LovePlants("Beach Don't Lose Plants", season, waveCount, baseDifficulty, 5);

            case DARK_AGES ->
                    new LockedPlants("Dark Ages Locked", season, waveCount, baseDifficulty, new ArrayList<>(), new ArrayList<>());
        };
    }
}