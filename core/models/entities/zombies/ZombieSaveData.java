package models.entities.zombies;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ZombieSaveData {

    @JsonProperty("type")
    private ZombieType type;

    public ZombieSaveData() {
    }

    public ZombieSaveData(ZombieType type) {
        this.type = type;
    }

    public ZombieType getType() {
        return type;
    }
}