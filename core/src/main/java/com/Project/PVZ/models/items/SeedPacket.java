package com.Project.PVZ.models.items;

import com.Project.PVZ.models.entities.plants.Plant;

public class SeedPacket implements Item {
    private final Plant desiredPlant;

    public SeedPacket(Plant desiredPlant) {
        this.desiredPlant = desiredPlant;
    }

    public Plant getDesiredPlant() {
        return desiredPlant;
    }
}
