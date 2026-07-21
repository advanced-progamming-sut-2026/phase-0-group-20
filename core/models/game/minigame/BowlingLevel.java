package models.game.minigame;

import models.App;
import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;
import models.game.adventure.levels.conditions.NormalLoseCondition;
import models.game.adventure.levels.conditions.NormalWinCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BowlingLevel extends Level {

    private final List<Plant> belt = new ArrayList<>();
    private final Random random = new Random();
    private static final int TICKS_PER_SECOND = 10;
    private static final int BELT_SPEED_SECONDS = 8;
    private static final int BELT_CAPACITY = 10;
    private static final int RED_LINE_COL = 3;

    public BowlingLevel(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int levelNumber) {
        super(name, season, waveCount, baseWaveDifficulty, levelNumber);
        this.addWinCondition(new NormalWinCondition());
        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onStart(GameSession session) {
        System.out.println("Bowling started, Plant behind the red line");
    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {
        super.engineLoop(session, currentTick);
        if (currentTick > 0 && currentTick % (BELT_SPEED_SECONDS * TICKS_PER_SECOND) == 0) {
            if (belt.size() < BELT_CAPACITY) {
                spawnPlantOnBelt();
            }
        }
    }


    private void spawnPlantOnBelt() {
        int rnd = random.nextInt(100);

        Plant template;

        if (rnd < 50) template = App.findPlantByName("wall-nut");  // chances: 50 - 25 - 25
        else if (rnd < 75) template = App.findPlantByName("explode-o-nut");
        else template = App.findPlantByName("tall-nut");

        Plant newPlant = PlantFactory.create(template.getId());
        belt.add(newPlant);
        System.out.println("A new " + newPlant.getName() + " arrived on the conveyor belt!");

    }

    public Plant consumePlant(int index) {
        if (index >= 0 && index < belt.size()) {
            return belt.remove(index);
        }
        return null;
    }

    public boolean isBehindRedLine(int col) {
        return col < RED_LINE_COL;
    }

    public List<Plant> getBelt() {
        return belt;
    }

    @Override
    public boolean skySunFalls() {
        return false;
    }

    @Override
    public boolean ignoresRecharge() {
        return true;
    }

    @Override
    public boolean skipsPlantSelection() {
        return true;
    }

    @Override
    public int getInitialSun() {
        return 0;
    }
}
