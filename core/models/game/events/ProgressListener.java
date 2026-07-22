package models.game.events;

import models.App;
import models.game.GameMode;
import models.game.GameSession;
import models.game.adventure.Adventure;
import models.game.minigame.IMinigame;
import models.game.minigame.MiniGameType;
import models.users.User;

public class ProgressListener implements GameEventListener {


    public ProgressListener() {
        registerToAllEvents();
    }

    private void registerToAllEvents() {
        GameEventMessenger messenger = GameEventMessenger.getInstance();
        for (GameEvent event : GameEvent.values())
            messenger.addListener(event, this);
    }

    public void unregisterFromAllEvents() {
        GameEventMessenger messenger = GameEventMessenger.getInstance();
        for (GameEvent event : GameEvent.values())
            messenger.removeListener(event, this);

    }


    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        switch (event) {
            case LEVEL_COMPLETED:
                handleLevelWon();
                break;
            case GAME_OVER:
                handelLevelLost();
                break;
            default:
                break;
        }
    }

    private void handleLevelWon() {
        User user = App.getActiveUser();
        if (user == null) return;

        user.setGamesPlayed(user.getGamesPlayed() + 1);

        GameMode currentMode = GameSession.getInstance().getCurrentMode();

        if (currentMode instanceof IMinigame miniGame) {
            MiniGameType type = miniGame.getMiniGameType();
            int before = user.getUnlockedLevelInMinigame(type);
            user.unlockNextLevelInMinigame(type);
            if (user.getUnlockedLevelInMinigame(type) > before)
                user.setLevelsCompleted(user.getLevelsCompleted() + 1);
        } else {
            Adventure adventure = App.getActiveAdventure();
            if (adventure != null) {
                int chapterIndex = adventure.getCurrentChapterIndex();
                int levelIndex = adventure.getCurrentChapter().getCurrentLevelIndex();

                if (chapterIndex == user.getHighestUnlockedChapterIndex() &&
                        levelIndex == user.getHighestUnlockedLevelIndex()) {

                    user.setLevelsCompleted(user.getLevelsCompleted() + 1);
                    adventure.onLevelWon();
                }
            }
        }

    }

    private void handelLevelLost() {
        User activeUser = App.getActiveUser();
        if (activeUser != null)
            activeUser.setGamesPlayed(activeUser.getGamesPlayed() + 1);
    }
}