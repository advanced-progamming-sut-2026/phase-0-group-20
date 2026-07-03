package models.game;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private final int ROWS = 5;
    private final int COLS = 9;

    private final List<Plant> activePlants;
    private final List<Zombie> activeZombies;


    public Arena() {
        this.activePlants = new ArrayList<>();
        this.activeZombies = new ArrayList<>();
    }

    public void addZombie(Zombie zombie) {
        activeZombies.add(zombie);
    }

    public void addPlant(Plant plant) {
        activePlants.add(plant);
    }

    public List<Zombie> getZombiesInRadius(int column, int lane, double radius) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : activeZombies) {
            if (zombie.isDead()) continue;
            double dx = zombie.getX() - column;
            double dy = zombie.getRow() - lane;
            if (Math.sqrt(dx * dx + dy * dy) <= radius) result.add(zombie);
        }
        return result;
    }

    public List<Plant> getActivePlants() { return activePlants; }
    public List<Zombie> getActiveZombies() { return activeZombies; }
    public int getRows() { return ROWS; }
    public int getCols() { return COLS; }
}
