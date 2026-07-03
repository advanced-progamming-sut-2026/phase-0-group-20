package models.game;

import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.fields.LawnMower;
import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public class GameSession {
    private TimeManager timeManager;
    private Arena arena;
    private Chapter currentChapter;
    private boolean isGameOver = false;

    private final List<Plant> activePlants;
    private final List<Zombie> activeZombies;
    private final List<Projectile> activeProjectiles = new ArrayList<>();
    private final List<LawnMower> lawnMowers = new ArrayList<>();

    private Sun sun;
    private Wave waveManager;


    public GameSession(Chapter chapter, Arena arena) {
        this.currentChapter = chapter;
        this.arena = arena;
        this.timeManager = new TimeManager();
        activePlants = arena.getActivePlants();
        activeZombies = arena.getActiveZombies();
    }


    public void spawnZombie(Zombie z, int lane) {

    }

    public void spawnPlant(Plant plant) {

    }

    public void startGame() {
        while (!isGameOver) {
            timeManager.tick();


            for (Plant p : activePlants) p.onTick(timeManager.getCurrentTick());
            for (Zombie z : activeZombies) z.onTick(timeManager.getCurrentTick());
            for (Projectile proj : activeProjectiles) proj.move();

            checkCollisions();
        }
    }

    public List<Zombie> zombieInRow(int row) {
        List<Zombie> zombies = new ArrayList<>();
        for (Zombie z : activeZombies) {
            if (z.getRow() == row) {
                zombies.add(z);
            }
        }
        return zombies;
    }

    public void checkCollisions() {}

    public void addProjectile(Projectile p) { activeProjectiles.add(p); }
    public void removeZombie(Zombie z) { activeZombies.remove(z); }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public Arena getArena() {
        return arena;
    }

    public Wave getWaveManager() {
        return waveManager;
    }

    public Sun getSun() {
        return sun;
    }

    public List<LawnMower> getLawnMowers() {
        return lawnMowers;
    }

    public List<Projectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    public List<Zombie> getActiveZombies() {
        return activeZombies;
    }

    public List<Plant> getActivePlants() {
        return activePlants;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public Chapter getCurrentChapter() {
        return currentChapter;
    }
}
