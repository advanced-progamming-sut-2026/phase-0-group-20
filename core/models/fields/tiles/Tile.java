package models.fields.tiles;

import models.Position;
import models.entities.plants.Plant;
import models.enums.plants.PlantTag;
import models.game.adventure.SeasonType;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;

public abstract class Tile implements Ticker {
    protected SeasonType currentSeason;
    protected List<Plant> plants = new ArrayList<>();
    protected Position position;

    public Tile(int row, int col) {
        position = new Position(col, row);
    }

    public abstract void onTick(int currentTick);

    public int getCol() {
        return position.getCol();
    }

    public int getRow() {
        return position.getRow();
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(List<Plant> plants) {
        this.plants = plants;
    }

    public void addPlant(Plant plant) {
        this.plants.add(plant);
        plant.setPlacedTile(this);
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
