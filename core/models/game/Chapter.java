package models.game;

import models.game.adventure.levels.Level;

import java.util.List;

public class Chapter {
    private final String chapterName;
    private final List<Level> levels;
    private int currentLevelIndex;

    public Chapter(String chapterName, List<Level> levels) {
        this.chapterName = chapterName;
        this.levels = levels;
        this.currentLevelIndex = 0;
    }

    public Level getCurrentLevel() {
        if (currentLevelIndex < levels.size()) {
            return levels.get(currentLevelIndex);
        }
        return null;
    }

    public void advanceToNextLevel() {
        currentLevelIndex++;
    }

    public String getChapterName() { return chapterName; }
}
