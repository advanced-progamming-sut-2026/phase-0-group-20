package models.entities.plants;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PlantSaveData {

    @JsonProperty("id")
    private int id;

    @JsonProperty("level")
    private int level;

    @JsonProperty("boosted")
    private boolean boosted;

    public PlantSaveData() {
    }

    public PlantSaveData(int id, int level, boolean boosted) {
        this.id = id;
        this.level = level;
        this.boosted = boosted;
    }

    public int getId() {
        return id;
    }

    public int getLevel() {
        return level;
    }

    public boolean isBoosted() {
        return boosted;
    }
}