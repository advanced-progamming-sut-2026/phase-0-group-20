package models.game;

import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private final int ROWS = 5;
    private final int COLS = 9;

    private final List<Plant> plants;
    private final List<Zombie> zombies;
    private final List<Projectile> projectiles;


    public Arena() {
        this.plants = new ArrayList<>();
        this.zombies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
    }

    public void addZombie(Zombie zombie) {
        zombies.add(zombie);
    }

    public void addPlant(Plant plant) {
        plants.add(plant);
    }

    public List<Plant> getActivePlants() { return plants; }
    public List<Zombie> getActiveZombies() { return zombies; }
    public List<Projectile> getActiveProjectiles() { return projectiles; }
    public int getRows() { return ROWS; }
    public int getCols() { return COLS; }
}
