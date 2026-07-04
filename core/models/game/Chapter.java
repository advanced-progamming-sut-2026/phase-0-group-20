package models.game;

import models.fields.modifiers.*;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;

import java.util.List;

public class Chapter {
    private final SeasonType type;
    private final SeasonModifier modifier;
    private final List<Level> levels;
    private int currentLevelIndex;
    private boolean isUnlocked;

    public Chapter(List<Level> levels, SeasonType type) {
        this.levels = levels;
        this.currentLevelIndex = 0;
        this.type = type;
        this.modifier = createModifier(type);
        this.isUnlocked = false;
    }

    public Level getCurrentLevel() {
        if (currentLevelIndex < levels.size()) {
            return levels.get(currentLevelIndex);
        }
        return null;
    }

    public static SeasonModifier createModifier(SeasonType type) {
        return switch (type) {
            case ANCIENT_EGYPT -> new EgyptModifier();
            case FROZEN_CAVES -> new IceCaveModifier();
            case BIG_WAVE_BEACH -> new BigWaveModifier();
            case DARK_AGES -> new DarkAgesModifier();
        };
    }

    public static String displayName(SeasonType type) {
        return switch (type) {
            case ANCIENT_EGYPT -> "Ancient Egypt";
            case FROZEN_CAVES -> "Frozen Caves";
            case BIG_WAVE_BEACH -> "Big Wave Beach";
            case DARK_AGES -> "Dark Ages";
        };
    }

    public boolean isNight() {
        return type == SeasonType.DARK_AGES;
    }

    public SeasonType getType() {
        return type;
    }

    public SeasonModifier getModifier() {
        return modifier;
    }

    public String getDisplayName() {
        return displayName(type);
    }

    public SeasonType getSeasonType() {
        return type;
    }

    public SeasonModifier getSeasonModifier() {
        return modifier;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.isUnlocked = unlocked;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void advanceToNextLevel() {
        currentLevelIndex++;
    }

}
