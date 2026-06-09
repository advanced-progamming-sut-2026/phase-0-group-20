package models.game;

import models.timeManager.TimeManager;

public class Game {
    private TimeManager timeManager;
    //pseudo: make it singleton if it doesn't interfere with the rest of the project.
    public TimeManager getTimeManager() {
        return timeManager;
    }

}
