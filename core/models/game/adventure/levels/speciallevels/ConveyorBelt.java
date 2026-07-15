package models.game.adventure.levels.speciallevels;

import models.App;
import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.SpecialLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ConveyorBelt extends SpecialLevel {
    private final List<Plant> belt = new ArrayList<>();
    private List<Plant> unlockedPlants;
    private final Random random = new Random();
    private static final int TICKS_PER_SECOND = 10;
    private static final int BELT_SPEED_SECONDS = 13;
    private static final int BELT_CAPACITY = 10;

    protected ConveyorBelt(String name, SeasonType season, int waveCount, int baseWaveDifficulty) {
        super(name, season, waveCount, baseWaveDifficulty);
    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {
        if (currentTick > 0 && currentTick % (BELT_SPEED_SECONDS * TICKS_PER_SECOND) == 0) {
            if (belt.size() < BELT_CAPACITY) {
                spawnPlantOnBelt();
            }
        }
    }

    @Override
    public void onStart(GameSession session) {
        unlockedPlants = new ArrayList<>(App.getActiveUser().getUnlockedPlants());
    }

    @Override
    public boolean skySunFalls() {
        return false;
    }

    @Override
    public boolean ignoresRecharge() {
        return false;
    }

    @Override
    public boolean skipsPlantSelection() {
        return false;
    }

    @Override
    public int getInitialSun() {
        return 0;
    }

    private void spawnPlantOnBelt() {
        if (unlockedPlants == null || unlockedPlants.isEmpty()) return;

        Plant template = unlockedPlants.get(random.nextInt(unlockedPlants.size()));

        Plant newPlant = PlantFactory.create(template.getId());

        belt.add(template);
        System.out.println("A new " + template.getName() + " arrived on the conveyor belt!");
    }

    public List<Plant> getBelt() {
        return belt;
    }

    public Plant consumePlant(int index) {
        if (index >= 0 && index < belt.size()) {
            return belt.remove(index);
        }
        return null;
    }
}
