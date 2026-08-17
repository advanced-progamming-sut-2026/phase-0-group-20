package io.java.pvz.models.fields.tiles;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.List;

public abstract class Tile implements Ticker {
    protected SeasonType currentSeason;
    protected ArrayList<Plant> plants = new ArrayList<>();
    protected Position position;
    protected boolean isCrater;
    protected boolean isFired =false;
    protected int craterTimer = 0;

    public Tile(int row, int col) {
        position = new Position(col, row);
    }

    public void onTick(int currentTick) {
        updateCraterLogic();
        customTick(currentTick);
    }

    protected abstract void customTick(int currentTick);

    public int getCol() {
        return position.getCol();
    }

    public int getRow() {
        return position.getRow();
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public void setPlants(ArrayList<Plant> plants) {
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
        if (isCrater) return false;
        boolean isWaterPlant = plantToPlant.getTags().contains(PlantTag.WATER);

        if (isWaterPlant) return false;
        boolean isGraveBuster = this instanceof GraveStoneTile tile && tile.getGraveStone() != null &&
                (plantToPlant.getName().contains("Buster")|| plantToPlant.getName().contains("buster"));
        return this.plants.isEmpty() || plantToPlant.getTags().contains(PlantTag.STACK) || isGraveBuster ||
                plantToPlant.getName().equalsIgnoreCase("hot potato");
    }

    public void removePlant(Plant plant) {
        this.plants.remove(plant);
    }


    public String getType() {
        String className = getClass().getSimpleName();

        if (className.equals("GraveStoneTile")) {
            return "GraveStone";
        }
        if (className.equals("NecromanceTile")) {
            return "NecromancyTile";
        }

        return className;
    }

    public boolean isCrater() {return isCrater;}
    public void setCrater(boolean isCrater) {this.isCrater = isCrater;}
    public void setCraterTimer(int ticks) {this.craterTimer = ticks;}
    public int getCraterTimer() {return craterTimer;}
    protected void updateCraterLogic() {
        if (isCrater && craterTimer > 0) {
            craterTimer--;
            if (craterTimer <= 0) {
                isFired = false;
                isCrater = false;
                GameSession.notify("Creator is Back to Normal Tile.");
            }
        }
    }

    public Plant getStackPlant() {
        for (Plant plant : plants) if (plant.getTags().contains(PlantTag.STACK)) return plant;
        return null;
    }

    public void setFired(boolean isFired) {this.isFired = isFired;}

    public boolean isFired() {
        return isFired;
    }
}
