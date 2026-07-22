package models.users;

import models.entities.plants.Plant;

import java.util.HashMap;

public class Inventory {
    private HashMap<String, Integer> foodPlants = new HashMap<>();
    private HashMap<String, Integer> seedPackets = new HashMap<>();

    public Inventory() {
    }

    public HashMap<String, Integer> getFoodPlants() {
        return foodPlants;
    }

    public void setFoodPlants(HashMap<String, Integer> foodPlants) {
        this.foodPlants = foodPlants;
    }

    public HashMap<String, Integer> getSeedPackets() {
        return seedPackets;
    }

    public void setSeedPackets(HashMap<String, Integer> seedPackets) {
        this.seedPackets = seedPackets;
    }

    public void addFoodPlant(Plant plant) {
        String key = plant.getName();
        foodPlants.merge(key, 1, Integer::sum);
    }

    public void addSeedPacket(Plant plant) {
        String key = plant.getName();
        seedPackets.merge(key, 1, Integer::sum);
    }

    public void addSeedPacket(Plant plant , int amount){
        String key = plant.getName();
        seedPackets.computeIfPresent(key, (k,v)->v+amount);
    }

    public void costFoodPlant(Plant plant, int amount) {
        String key = plant.getName();
        foodPlants.computeIfPresent(key, (k, v) -> v - amount);
    }

    public void costSeedPacket(Plant plant, int amount) {
        String key = plant.getName();
        seedPackets.computeIfPresent(key, (k, v) -> v - amount);
    }
}
