package models.game.minigame;

import models.fields.Brain;
import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;

import java.util.Random;

public class IZombieLevel extends Level {

    private final Random rand = new Random();
    private static final int RED_LINE_COL = 5;

    public IZombieLevel(String name, int levelNumber) {
        super(name, SeasonType.DARK_AGES, 1, -1, levelNumber);
        // this.addWinCondition(new IZombieWinCondition());
        // this.addLoseCondition(new IZombieLoseCondition());
    }

    @Override
    public void onStart(GameSession session) {

    }

    private void spawnPrePlacedPlants(GameSession session, int row) {
        //logic for placing plant
    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {
        // we don't have spawn wave
    }

    public boolean isValidZombiePlacement(int col) {
        return col >= RED_LINE_COL;
    }

    @Override
    public int getInitialSun() {
        return 150;
    }

    @Override
    public boolean skySunFalls() {
        return false;
    }
}