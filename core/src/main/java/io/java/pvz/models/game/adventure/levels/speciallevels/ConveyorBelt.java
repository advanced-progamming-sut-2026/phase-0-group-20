package io.java.pvz.models.game.adventure.levels.speciallevels;

import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantFactory;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.SpecialLevel;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ConveyorBelt extends SpecialLevel {
    private static final int BELT_SPEED_SECONDS = 6;
    private static final int BELT_CAPACITY = 10;
    private final List<Plant> belt = new ArrayList<>();
    private final Random random = new Random();
    private List<Plant> unlockedPlants;

    public ConveyorBelt(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {

        super.engineLoop(session, currentTick);

        if (currentTick > 0 && currentTick % (BELT_SPEED_SECONDS * TimeManager.TICKS_PER_SECOND) == 0) {
            if (belt.size() < BELT_CAPACITY) {
                spawnPlantOnBelt();
            }
        }
    }

    @Override
    public void onLevelStart(GameSession session) {
        unlockedPlants = new ArrayList<>(App.getActiveUser().getUnlockedPlants());
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

    protected void spawnPlantOnBelt() {
        if (unlockedPlants == null || unlockedPlants.isEmpty()) return;
        Plant template;
       do{
           template = unlockedPlants.get(random.nextInt(unlockedPlants.size()));

       }while(template.getCategory() == PlantCategory.SUN_PRODUCER);
        Plant newPlant = PlantFactory.create(template.getId());

        belt.add(newPlant);
        notify("A new " + newPlant.getName() + " arrived on the conveyor belt!");
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
