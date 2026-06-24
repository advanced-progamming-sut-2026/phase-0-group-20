package models.items;

import models.entities.plants.Plant;

public class SeedPacket implements Item {
    private final Plant desiredPlant;

    public SeedPacket(Plant desiredPlant) {
        this.desiredPlant = desiredPlant;
    }

    public Plant getDesiredPlant() {
        return desiredPlant;
    }
}
