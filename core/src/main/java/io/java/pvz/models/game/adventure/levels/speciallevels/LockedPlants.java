package io.java.pvz.models.game.adventure.levels.speciallevels;

import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.SpecialLevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LockedPlants extends SpecialLevel {
    private static final int NUMBER_OF_LOCKED_PLANTS = 1 ;
    private static final int NUMBER_OF_LOCKED_SLOTS = 2 ;

    private  List<PlantCategory> lockedCategories;
    private  Plant forcedToUsePlant;
    private final int mode;
    private int lockedSlots = 0;

    public LockedPlants(String name, SeasonType season, int waveCount, int baseWaveBudget, int globalLevelNumber,
                        int mode) {
        super(name, season, waveCount, baseWaveBudget, globalLevelNumber);
        this.forcedToUsePlant = null;
        this.lockedCategories =  new ArrayList<>();
        this.mode = mode;
    }

    @Override
    public void onLevelStart(GameSession session) {
        // Initialization logic
    }

    @Override
    public boolean isPlantAllowed(Plant plant) {
        return lockedCategories.stream()
                .noneMatch(category -> category == plant.getCategory());
    }

    public List<PlantCategory> getLockedCategories() {
        return lockedCategories;
    }

    public Plant getForcedToUsePlant() {
        return forcedToUsePlant;
    }

    public void createModEntities(){
        switch(mode){
            case 1->{// locked categories mode
                lockedCategories = getRandomCategories();
            }
            case 2->{// locked slots
                lockedSlots = (App.getSettings().getDifficulty()>=4)? NUMBER_OF_LOCKED_SLOTS+1 : NUMBER_OF_LOCKED_SLOTS;
            }
            default -> {//forced to use plants
                forcedToUsePlant = getRandomPlant();
            }
        }
    }

    private static List<PlantCategory> getRandomCategories() {
        List<PlantCategory> allCategories = new ArrayList<>(Arrays.asList(PlantCategory.values()));
        Collections.shuffle(allCategories);

        return allCategories.subList(0, Math.min(NUMBER_OF_LOCKED_PLANTS, allCategories.size()));
    }

    private static Plant getRandomPlant(){
        List <Plant> unlockedPlants = App.getActiveUser().getUnlockedPlants();
        Collections.shuffle(unlockedPlants);

        return unlockedPlants.get(0);
    }

    public String createMessage() {
        return switch (mode) {
            case 1 -> {
                String categories = lockedCategories.toString();
                yield "Family Restriction: The following plant categories are banned in this level: " + categories;
            }
            case 2 -> {
                yield "Locked Slots: " + lockedSlots + " of your seed slots are permanently locked for this level.";
            }
            default -> {
                String plantName = (forcedToUsePlant != null) ?
                        forcedToUsePlant.getName(): "a specific plant";
                yield "Forced Plant: You are forced to include " + plantName + " in your loadout. Good luck!";
            }
        };
    }

    public int getMode() {
        return mode;
    }

    public int getLockedSlots() {
        return lockedSlots;
    }
}
