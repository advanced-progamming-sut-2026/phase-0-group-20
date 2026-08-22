package io.java.pvz.models.users;

import io.java.pvz.models.entities.plants.Plant;

import java.util.HashMap;

public class Inventory {
    private HashMap<String, Integer> seedPackets = new HashMap<>();

    public Inventory() {
    }

    public HashMap<String, Integer> getSeedPackets() {
        return seedPackets;
    }

    public void addSeedPacket(Plant plant) {
        String key = plant.getName();
        seedPackets.merge(key, 1, Integer::sum);
    }

    public void addSeedPacket(Plant plant, int amount) {
        String key = plant.getName();
        seedPackets.merge(key, amount, Integer::sum);
    }
}
