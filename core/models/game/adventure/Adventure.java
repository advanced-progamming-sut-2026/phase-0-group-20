package models.game.adventure;

import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;
import models.users.User;

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

        models.users.User currentUser = models.App.getActiveUser();
        int unlockedChapIndex = (currentUser != null) ? currentUser.getHighestUnlockedChapterIndex() : 0;
        int unlockedLevelIndex = (currentUser != null) ? currentUser.getHighestUnlockedLevelIndex() : 0;

        for (int i = 0; i < seasons.length; i++) {
            Chapter chapter = new Chapter(seasons[i]);

            if (i <= unlockedChapIndex) {
                chapter.setUnlocked(true);
                if (i == unlockedChapIndex)
                    chapter.setCurrentLevelIndex(unlockedLevelIndex);
                else
                    chapter.setCurrentLevelIndex(0);

            } else
                chapter.setUnlocked(false);

            chapters.add(chapter);
        }

        this.currentChapterIndex = unlockedChapIndex;
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

    public void onLevelWon() {

        Chapter currentChap = chapters.get(currentChapterIndex);
        User currentUser = models.App.getActiveUser();
        currentChap.advanceToNextLevel();

        if (currentChap.getCurrentLevelIndex() > 3) {
            unlockNextChapter();
            if (currentUser != null) {
                currentUser.setHighestUnlockedChapterIndex(currentChapterIndex);
                currentUser.setHighestUnlockedLevelIndex(0);
            }
        } else {
            if (currentUser != null) {
                if (currentChapterIndex >= currentUser.getHighestUnlockedChapterIndex() &&
                        currentChap.getCurrentLevelIndex() > currentUser.getHighestUnlockedLevelIndex())
                    currentUser.setHighestUnlockedLevelIndex(currentChap.getCurrentLevelIndex());

            }
        }
    }
}
