package io.java.pvz.models.game.adventure.levels.speciallevels;

import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantFactory;
import io.java.pvz.models.entities.plants.PlantCategory;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.SpecialLevel;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class ConveyorBelt extends SpecialLevel {
    private static final int BELT_SPEED_SECONDS = 6;
    private static final int BELT_CAPACITY = 10;
    private final List<Plant> belt = new ArrayList<>();
    protected final Random random = new Random();
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
        List<String> plantPool = new ArrayList<>(Arrays.asList(
            "Peashooter", "Wall-nut", "Cabbage-pult",
            "Snow Pea", "Repeater", "Snapdragon",
            "Cherry Bomb", "Bonk Choy", "Threepeater",
            "Melon-pult", "Kernel-pult", "Cactus",
            "Wasabi Whip", "Torchwood", "Chomper"
        ));
        Plant template;
        Plant newPlant = null;

        do {
            String randomPlantName = plantPool.get(random.nextInt(plantPool.size()));
            template = App.findPlantByName(randomPlantName);

            if (template != null &&
                template.getCategory() != PlantCategory.SUN_PRODUCER &&
                App.getActiveUser().isItUnlocked(template)) {
                newPlant = PlantFactory.create(template.getId());
            }
        } while (newPlant == null);

        getBelt().add(newPlant);
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

    @Override
    public String toString() {
        return "Don't Let Zombies Eat Your Brain";
    }
}
