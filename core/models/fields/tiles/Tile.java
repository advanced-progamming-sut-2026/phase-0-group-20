package models.fields.tiles;

import models.entities.plants.Plant;
import models.enums.plants.PlantTag;
import models.game.adventure.SeasonType;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;

public abstract class Tile implements Ticker {
    protected SeasonType currentSeason;
    protected List<Plant> plants = new ArrayList<>();
    protected int row;
    protected int col;

    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public abstract void onTick(int currentTick);

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public void addPlant(Plant plant) {
        this.plants.add(plant);
    }

    public SeasonType getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(SeasonType currentSeason) {
        this.currentSeason = currentSeason;
    }

    public boolean isPlantable(Plant plantToPlant) {
        boolean isWaterPlant = plantToPlant.getTags().contains(PlantTag.WATER);

        if (isWaterPlant) return false;

        return this.plants.isEmpty() || plantToPlant.getTags().contains(PlantTag.STACK);
    }
}
