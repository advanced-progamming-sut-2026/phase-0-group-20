package io.java.pvz.models.game.adventure;

import io.java.pvz.models.fields.modifiers.*;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.adventure.levels.LevelFactory;

import java.util.ArrayList;
import java.util.List;

public class Chapter {
    private final SeasonType type;
    private final SeasonModifier modifier;
    private final List<Level> levels;
    private int maxLevelIndexInThisChapter;
    private boolean isUnlocked;
    private int chapterIndex;

    public Chapter(SeasonType type) {
        this.levels = new ArrayList<>();
        this.maxLevelIndexInThisChapter = 0;
        this.type = type;
        this.modifier = createModifier(type);
        this.isUnlocked = false;
        createLevels();
        chapterIndex = switch (type) {
            case ANCIENT_EGYPT -> 0;
            case FROZEN_CAVES -> 1;
            case DARK_AGES -> 2;
            case BIG_WAVE_BEACH -> 3;
            case MINI_GAME -> 4;
        };
    }

    public static SeasonModifier createModifier(SeasonType type) {
        return switch (type) {
            case ANCIENT_EGYPT -> new EgyptModifier();
            case FROZEN_CAVES -> new IceCaveModifier();
            case BIG_WAVE_BEACH -> new BigWaveModifier();
            case DARK_AGES -> new DarkAgesModifier();
            case MINI_GAME -> new MiniGameModifier();
        };
    }

    public static String displayName(SeasonType type) {
        return switch (type) {
            case ANCIENT_EGYPT -> "Ancient Egypt";
            case FROZEN_CAVES -> "Frozen Caves";
            case BIG_WAVE_BEACH -> "Big Wave Beach";
            case DARK_AGES -> "Dark Ages";
            case MINI_GAME -> "Mini Game";
        };
    }

    private void createLevels() {
        for (int i = 0; i < 4; i++) {
            Level newLevel = LevelFactory.createLevel(this.type, i);
            this.levels.add(newLevel);
        }
    }

    public Level getCurrentLevel() {
        if (currentLevelIndex == 4) {
            currentLevelIndex = 3;
        }
        return levels.get(currentLevelIndex);
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
        if (unlocked) for (int i = 0; i < levels.size(); i++) advanceToNextLevel();
    }

    public List<Level> getLevels() {
        return levels;
    }

    public void advanceToNextLevel() {
        if (maxLevelIndexInThisChapter < levels.size() - 1) maxLevelIndexInThisChapter++;
    }

    public int getMaxLevelIndexInThisChapter() {
        return maxLevelIndexInThisChapter;
    }

    public void setMaxLevelIndexInThisChapter(int maxLevelIndexInThisChapter) {
        this.maxLevelIndexInThisChapter = maxLevelIndexInThisChapter;
    }

    public int getChapterIndex() {
        return chapterIndex;
    }
}
