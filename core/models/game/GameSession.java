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
    private static GameSession instance;
    private TimeManager timeManager;
    private Arena arena;
    private Chapter currentChapter;
    private boolean isGameOver = false;

    private final List<Plant> activePlants;
    private final List<Zombie> activeZombies;
    private final List<Projectile> activeProjectiles = new ArrayList<>();
    private final List<LawnMower> lawnMowers = new ArrayList<>();
    private final List <Sun> activeSuns = new ArrayList<>();

    private List<WinCondition> winConditions;
    private List<LoseCondition> loseConditions;

    private Wave waveManager;


    private GameSession(Chapter chapter, Arena arena) {
        this.currentChapter = chapter;
        this.arena = arena;
        this.timeManager = new TimeManager();
        activePlants = arena.getActivePlants();
        activeZombies = arena.getActiveZombies();
    }

    public static GameSession getInstance(){
        if(instance == null){
            System.out.println("Instance is null");
        }
        return instance;
    }

    public static GameSession getInstance(Chapter chapter, Arena arena){
        if(instance == null){
            instance = new GameSession(chapter, arena);
        }
        return instance;
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

    public void update(int timeAmount) {
        for(int i = 0; i < timeAmount; i++){

            timeManager.tick();
            removeDeadEntities();
            checkGameConditions();
        }
    }

    private void removeDeadEntities() {

        arena.getActiveZombies().removeIf(zombie -> {
            if (zombie.isDead()) {
                timeManager.unregisterTicker(zombie);
                return true;
            }
            return false;
        });


        arena.getActivePlants().removeIf(plant -> {
            if (plant.getCurrentHp() <= 0) {
                timeManager.unregisterTicker(plant);
                return true;
            }
            return false;
        });
    }

    private void checkGameConditions() { // check for win or lose to end the game
        for (LoseCondition lose : loseConditions) {
//            if (lose.isLost(this)) {
//                isGameOver = false;
//                return;
//            }
        }

        for (WinCondition win : winConditions) {
//            if (win.isWon(this)) {
//                isGameOver   = false;
//                return;
//            }
        }
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
