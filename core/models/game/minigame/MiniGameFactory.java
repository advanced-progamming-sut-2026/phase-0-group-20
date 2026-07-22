package models.game.minigame;

import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;

import static models.enums.GameConstants.BASE_WAVE_COST;
import static models.enums.GameConstants.LEVEL_EFFECTS_SCALE;

public class MiniGameFactory {
    private static final int WAVE_FREE_WAVE_COUNT = 1;

    public static Level createLevel(MiniGameType type, int levelNumber) {
        int waveCount = 2 + (levelNumber / 2);
        int waveBudget = BASE_WAVE_COST + (levelNumber * LEVEL_EFFECTS_SCALE);

        return switch (type) {
            case VASE_BREAKER -> new VaseBreakerLevel(
                    getLevelName(type, levelNumber),
                    SeasonType.MINI_GAME,
                    WAVE_FREE_WAVE_COUNT,
                    levelNumber
            );

            case I_ZOMBIE -> new IZombieLevel(
                    getLevelName(type, levelNumber),
                    SeasonType.MINI_GAME,
                    WAVE_FREE_WAVE_COUNT,
                    levelNumber
            );

            case BOWLING -> new BowlingLevel(
                    getLevelName(type, levelNumber),
                    SeasonType.ANCIENT_EGYPT,
                    waveCount,
                    waveBudget,
                    levelNumber
            );

            case BEGHOULED -> new BeghouledLevel(
                    getLevelName(type, levelNumber),
                    SeasonType.MINI_GAME,
                    WAVE_FREE_WAVE_COUNT, waveBudget,
                    levelNumber
            );

            case ZOMBOTANY -> new ZombotanyLevel(
                    MiniGameType.ZOMBOTANY.getName() + " level " + levelNumber,
                    SeasonType.MINI_GAME,
                    waveCount,
                    waveBudget,
                    levelNumber
            );
        };
    }

    private static String getLevelName(MiniGameType miniGameType, int levelNumber) {
        return miniGameType.getName() + " Level " + levelNumber;
    }
}