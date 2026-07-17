package models.greenhouse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.App;
import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.entities.plants.PlantSaveData;
import models.timeManager.Ticker;

import java.util.List;
import java.util.Random;

public class Pot implements Ticker {

    @JsonProperty("potCondition")
    private PotCondition potCondition = PotCondition.EMPTY;

    @JsonIgnore
    private Plant plantedPlant = null;

    @JsonProperty("readyTime")
    private long readyTime = 0;;

    @JsonProperty("isItMari")
    private boolean isItMari = false;

    private static final long TIME_TO_HATCH_MARIGOLD = 7200000L;
    private static final long TIME_TO_HATCH_PLANT = 28800000L;

    public Pot() {
    }

    @Override
    public void onTick(int currentTick) {
        if (potCondition == PotCondition.PLANTED) {
            if (System.currentTimeMillis() >= readyTime) {
                potCondition = PotCondition.COLLECTABLE;
            }
        }
    }

    public void plantPlant() {
        isItMari = getTheType();
        long currentTime = System.currentTimeMillis();

        if (isItMari) {
            readyTime = currentTime + TIME_TO_HATCH_MARIGOLD;
            potCondition = PotCondition.PLANTED;
        } else {
            Plant plant = getRandomUnlockedPlant();
            plantedPlant = plant;

            readyTime = currentTime + TIME_TO_HATCH_PLANT;
            potCondition = PotCondition.PLANTED;
        }
    }

    public void collectPlant() {
        this.potCondition = PotCondition.EMPTY;
        this.plantedPlant = null;
        this.isItMari = false;
    }

    public void growFaster(int x, int y) {

    }

    public PotCondition getPotCondition() {
        return potCondition;
    }

    public void setPotCondition(PotCondition potCondition) {
        this.potCondition = potCondition;
    }

    @JsonIgnore
    public Plant getPlantedPlant() {
        return plantedPlant;
    }

    @JsonIgnore
    public void setPlantedPlant(Plant plantedPlant) {
        this.plantedPlant = plantedPlant;
    }

    @JsonProperty("plantedPlantData")
    public PlantSaveData getPlantedPlantData() {
        if (plantedPlant == null) {
            return null;
        }
        return new PlantSaveData(plantedPlant.getId(), plantedPlant.getLevel(), plantedPlant.isBoosted());
    }

    @JsonProperty("plantedPlantData")
    public void setPlantedPlantData(PlantSaveData data) {
        if (data == null) {
            this.plantedPlant = null;
        } else {
            this.plantedPlant = models.entities.plants.PlantFactory.create(data.getId());
            if (this.plantedPlant != null) {
                this.plantedPlant.setBoosted(data.isBoosted());

                while (this.plantedPlant.getLevel() < data.getLevel()) {
                    this.plantedPlant.upgrade();
                }
            }
        }
    }

    @JsonIgnore
    public String getFormattedRemainingTime() {
        if (potCondition != PotCondition.PLANTED) return "Ready";

        long remainingMilli = readyTime - System.currentTimeMillis();

        if (remainingMilli <= 0) return "Ready";

        long totalSeconds = remainingMilli / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        else {
            return String.format("%d:%02d", minutes, seconds);
        }
    }

    public long getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(long readyTime) {
        this.readyTime = readyTime;
    }

    @JsonIgnore
    public boolean isItMari() {
        return isItMari;
    }

    @JsonIgnore
    public void setItMari(boolean itMari) {
        isItMari = itMari;
    }

    private boolean getTheType() {
        Random rand = new Random();
        int chance = rand.nextInt(100);
        if (chance < 50 || App.getActiveUser().getUnlockedPlants().isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private Plant getRandomUnlockedPlant() {
        List<Plant> plants = App.getActiveUser().getUnlockedPlants();
        Random rand = new Random();
        int index = rand.nextInt(plants.size());
        return PlantFactory.create(plants.get(index).getId());
    }
}