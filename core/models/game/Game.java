package models.game;

import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private static Game instance;
    private TimeManager timeManager;
    private Arena arena;
    private Chapter currentChapter;

    private int sunCurrency;
    private boolean isPlaying;

    private List<WinCondition> winConditions;
    private List<LoseCondition> loseConditions;

    private Game() {
        this.timeManager = new TimeManager();
        this.arena = new Arena();
        this.winConditions = new ArrayList<>();
        this.loseConditions = new ArrayList<>();
        this.sunCurrency = 50; // initial amount of sun
        this.isPlaying = false;
    }

    public static Game getInstance() { // Singleton
        if (instance == null) {
            instance = new Game();
        }
        return instance;
    }

    public void startLevel(Chapter chapter) {
        this.currentChapter = chapter;
        this.arena = new Arena();
        this.isPlaying = true;
        this.sunCurrency = 50;
    }

    public void update() {
        if (!isPlaying) return;

        // update all timer and entities

        checkGameConditions();
    }

    private void checkGameConditions() { // check for win or lose to end the game
        for (LoseCondition lose : loseConditions) {
            if (lose.isLost(this)) {
                isPlaying = false;
                return;
            }
        }

        for (WinCondition win : winConditions) {
            if (win.isWon(this)) {
                isPlaying = false;
                return;
            }
        }
    }

    public void modifySun(int amount) {
        this.sunCurrency += amount;
        if (this.sunCurrency < 0) this.sunCurrency = 0;
    }

    // Getters
    public TimeManager getTimeManager() { return timeManager; }
    public Arena getArena() { return arena; }
    public int getSunCurrency() { return sunCurrency; }
    public boolean isPlaying() { return isPlaying; }
}
