package models.game.minigame;

import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;

public class MiniGameFactory {

    public static Level createLevel(MiniGameType type, int levelNumber) {
        return switch (type) {
            case VASE_BREAKER ->
                    new VaseBreakerLevel("Vasebreaker Level " + levelNumber, levelNumber);
            case I_ZOMBIE ->
                    new IZombieLevel("iZombie Level " + levelNumber, levelNumber);
            case BOWLING ->
                    new BowlingLevel(
                            "Bowling Level " + levelNumber,
                            SeasonType.ANCIENT_EGYPT,
                            1,
                            10 + (levelNumber * 5),
                            levelNumber
                    );

            case BEGHOULED -> throw new UnsupportedOperationException("Beghouled is not implemented yet");
        };
    }
}