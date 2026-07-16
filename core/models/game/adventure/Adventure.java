package models.game.adventure;

import models.game.adventure.levels.Level;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

import java.util.ArrayList;
import java.util.List;

public class Adventure {
    private final List<Chapter> chapters;
    private int currentChapterIndex;

    public Adventure() {
        this.chapters = new ArrayList<>();
        this.currentChapterIndex = 0;
        initializeAdventure();
    }

    private void initializeAdventure() {
        // in this method we should initialize the levels


        SeasonType[] seasons = SeasonType.values();

        for (int i = 0; i < seasons.length; i++) {
            Chapter chapter = new Chapter(seasons[i], i);
            if (i == 0) chapter.setUnlocked(true);
            chapters.add(chapter);
        }
    }

    public Chapter findChapterByName(String name) {
        for (Chapter chapter : chapters) {
            if (chapter.getDisplayName().equalsIgnoreCase(name.trim())) {
                return chapter;
            }
        }
        return null;
    }

    public void unlockNextChapter() {
        if (currentChapterIndex + 1 < chapters.size()) {
            currentChapterIndex++;
            chapters.get(currentChapterIndex).setUnlocked(true);
            notify("New Chapter Unlocked: " + chapters.get(currentChapterIndex).getDisplayName());
        }
    }

    public void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public Chapter getCurrentChapter() {
        if (currentChapterIndex < chapters.size()) {
            return chapters.get(currentChapterIndex);
        }
        return null;
    }

    public int getCurrentChapterIndex() {
        return currentChapterIndex;
    }

    public void setCurrentChapterIndex(int index) {
        if (index >= 0 && index < chapters.size()) {
            this.currentChapterIndex = index;
        }
    }
}
