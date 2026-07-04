package models.fields.tiles;

import models.entities.plants.Plant;
import models.game.adventure.SeasonType;
import models.timeManager.Ticker;

import java.util.Collections;
import java.util.List;

public abstract class Tile implements Ticker {
    protected SeasonType currentSeason;
    protected List<Plant> plants = null;
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

    public void setPlants(Plant plants) {
        this.plants = Collections.singletonList(plants);
    }

    public SeasonType getCurrentSeason() {
        return currentSeason;
    }

    public void setCurrentSeason(SeasonType currentSeason) {
        this.currentSeason = currentSeason;
    }
}
