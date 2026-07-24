import models.App;
import models.entities.plants.Plant;
import models.entities.plants.PlantData;
import models.entities.plants.PlantFactory;
import models.entities.plants.PlantLoader;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieFactory;
import models.entities.zombies.ZombieType;
import models.game.events.DailyResetListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameInitializer {

    private static final String PLANTS_JSON_PATH = "core/resources/plants.json";
    private static final String ZOMBIES_JSON_PATH = "core/resources/zombies.json";

    public static void loadAllResources() {
        initPlants();
        initZombies();
        DailyResetListener dailyResetListener = new DailyResetListener();
    }

    private static void initPlants() {
        System.out.println("Loading plants...");
        List<PlantData> loadedPlants = PlantLoader.loadAll(PLANTS_JSON_PATH);
        ArrayList<Plant> plants = new ArrayList<>();

        if (loadedPlants.isEmpty()) {
            throw new RuntimeException("CRITICAL: Failed to load plant data. Halting startup.");
        }

        Map<Integer, PlantData> plantMap = new HashMap<>();
        for (PlantData plant : loadedPlants) {
            plantMap.put(plant.id(), plant);
        }

        new PlantFactory(plantMap);
        for (Integer id : plantMap.keySet()) {
            Plant newPlant = PlantFactory.create(id);
            if (newPlant == null)
                continue;
            plants.add(newPlant);
            System.out.println("Successfully loaded plant: " + newPlant.getName());
        }
        App.setAllPlants(plants);
        System.out.println("Successfully loaded " + App.getAllPlants().size() + " plants.");
    }

    private static void initZombies() {
        List<Zombie> loadedTestZombies = new ArrayList<>();
        System.out.println("Loading zombies...");

        try {
            ZombieFactory.init(ZOMBIES_JSON_PATH);
            System.out.println("Successfully loaded zombies into the Factory.");

            for (ZombieType type : ZombieType.values()) {

                Zombie testZombie = ZombieFactory.createTemplate(type);
                loadedTestZombies.add(testZombie);

                System.out.println("Successfully created: " + type.name());
            }

            System.out.println("Total unique zombies created: " + loadedTestZombies.size());

        } catch (Exception e) {
            throw new RuntimeException("CRITICAL: Failed to load zombie data. Halting startup.", e);
        }
    }
}