package models.fields.tiles;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.adventure.SeasonType;
import models.timeManager.Ticker;

public abstract class Tile implements Ticker {
    protected SeasonType currentSeason;
    protected Plant plantedPlant = null;
    protected Zombie zombieOnTheTile = null;
    protected int row;
    protected int col;

    public Tile(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public abstract void onTick(int currentTick);

    public void plantThePlant(Plant plant) {
    }

    public void produceSun() {
    }

    public void pluckThePlant() {

    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public Zombie getZombieOnTheTile() {
        return zombieOnTheTile;
    }

    public void setZombieOnTheTile(Zombie zombieOnTheTile) {
        this.zombieOnTheTile = zombieOnTheTile;
    }

    public Plant getPlantedPlant() {
        return plantedPlant;
    }

    public void setPlantedPlant(Plant plantedPlant) {
        this.plantedPlant = plantedPlant;
    }

    public SeasonType getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(SeasonType currentSeason) {
        this.currentSeason = currentSeason;
    }
}
