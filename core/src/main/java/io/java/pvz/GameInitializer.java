package io.java.pvz;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import io.java.pvz.controllers.AudioManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantData;
import io.java.pvz.models.entities.plants.PlantFactory;
import io.java.pvz.models.entities.plants.PlantLoader;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieFactory;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.adventure.Adventure;
import io.java.pvz.models.game.events.DailyResetListener;
import io.java.pvz.models.users.User;
import io.java.pvz.views.sound.MusicType;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameInitializer {

    private static final String PLANTS_JSON_RELATIVE = "resources/plants.json";
    private static final String ZOMBIES_JSON_RELATIVE = "resources/zombies.json";

    public static void loadGameData() {
        initPlants();
        initZombies();
    }

    public static void loadAllResources() {
        loadGameData();
//        DailyResetListener dailyResetListener = new DailyResetListener();
//        User stayedUser = DataBaseManager.getLoggedInUser();
//        if (stayedUser == null) {
//            App.setActiveAdventure(new Adventure());
//        }
//
//        if (stayedUser != null) {
//            System.out.println(stayedUser.getUsername());
//            App.setActiveUser(stayedUser);
//            App.setActiveAdventure(new Adventure());
//            System.out.println("Welcome back, " + stayedUser.getUsername() + "!");
//            App.setAllUsers(DataBaseManager.getAllUsers());
//        }
        AssetManager am = AssetLoader.getInstance().getAssetManager();
        am.finishLoading();
        AudioManager.getInstance().init(am);
    }

    private static void initPlants() {
        System.out.println("Loading plants...");
        String plantsPath = resolveResourcePath(PLANTS_JSON_RELATIVE);
        List<PlantData> loadedPlants = PlantLoader.loadAll(plantsPath);
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

    private static String resolveResourcePath(String relativePath) {
        String override = System.getProperty("pvz.resourcesDir");
        if (override != null) {
            File candidate = new File(override, relativePath);
            if (candidate.isFile()) return candidate.getPath();
        }

        File direct = new File(relativePath);
        if (direct.isFile()) return relativePath;

        List<File> searchRoots = new ArrayList<>();
        searchRoots.add(new File(".").getAbsoluteFile());
        try {
            File codeSource = new File(GameInitializer.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());
            searchRoots.add(codeSource.getAbsoluteFile());
        } catch (Exception ignored) {

        }

        List<String> tried = new ArrayList<>();
        for (File root : searchRoots) {
            File dir = root.isFile() ? root.getParentFile() : root;
            for (int depth = 0; dir != null && depth < 6; depth++, dir = dir.getParentFile()) {
                File candidate = new File(dir, relativePath);
                tried.add(candidate.getPath());
                if (candidate.isFile()) return candidate.getPath();

                File assetsCandidate = new File(dir, "assets/" + relativePath);
                tried.add(assetsCandidate.getPath());
                if (assetsCandidate.isFile()) return assetsCandidate.getPath();
            }
        }

        throw new RuntimeException("Could not find '" + relativePath + "'. Tried:\n  "
            + String.join("\n  ", tried)
            + "\nRun with -Dpvz.resourcesDir=<path to the folder containing 'resources/'> "
            + "or launch the process with the project root as its working directory.");
    }

    private static void initZombies() {
        ArrayList <Zombie> loadedTestZombies = new ArrayList<>();
        System.out.println("Loading zombies...");

        try {
            ZombieFactory.init(resolveResourcePath(ZOMBIES_JSON_RELATIVE));
            System.out.println("Successfully loaded zombies into the Factory.");

            for (ZombieType type : ZombieType.values()) {
                if(type == ZombieType.ZOMBOSS_BEACH || type == ZombieType.ZOMBOSS_EGYPT||
                    type == ZombieType.ZOMBOSS_DARK_AGES || type == ZombieType.ZOMBOSS_FROZEN_CAVES)
                    continue;
                Zombie testZombie = ZombieFactory.createTemplate(type);
                loadedTestZombies.add(testZombie);

                System.out.println("Successfully created: " + type.name());
            }

            System.out.println("Total unique zombies created: " + loadedTestZombies.size());
            App.setAllZombies(loadedTestZombies);
        } catch (Exception e) {
            throw new RuntimeException("CRITICAL: Failed to load zombie data. Halting startup.", e);
        }
    }
}
