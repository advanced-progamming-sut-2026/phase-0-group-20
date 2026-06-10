package models.fields;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.adventure.SeasonType;
import models.timeManager.Ticker;

public abstract class Tile implements Ticker {

    SeasonType currentSeason;
    Plant plantedPlant = null;
    Zombie zombieOnTheTile = null;

    public abstract void onTick(int currentTick);

    public void plantThePlant(Plant plant) {
    }

    public void produceSun() {
    }

    public void pluckThePlant() {

    }

}
