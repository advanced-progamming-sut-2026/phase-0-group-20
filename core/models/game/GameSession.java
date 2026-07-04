package models.game;

import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.enums.GameState;
import models.game.adventure.levels.Level;
import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameSession {
    private static GameSession instance;
    private final List<Plant> ChosenPlants;
    private final List<Zombie> chosenZombies;
    private final List<Projectile> activeProjectiles = new ArrayList<>();
    private TimeManager timeManager;
    private Arena arena;
    private Chapter currentChapter;
    private boolean isGameOver = false;
    private int currentSun;
    private GameEvent event = GameEvent.GAME_STARTED;
    private GameState state = GameState.RUNNING;
    private SunManager sunManager;
    private HashMap<Plant, Integer> plantsCooldown;
    private GameMode currentMode;

    private boolean zombieBreached = false;

    private Wave waveManager;


    private GameSession(Chapter chapter, Arena arena, List<Plant> chosenPlants, List<Zombie> chosenZombies) {
        this.currentChapter = chapter;
        this.arena = arena;
        this.timeManager = new TimeManager();
        this.ChosenPlants = chosenPlants;// this should come from PlantSelectionMenu Not the arena
        plantsCooldown = new HashMap<>();
        instantiateCooldowns(chosenPlants);
        this.chosenZombies = chosenZombies;// this should come from our level Modifiers or something else
        this.currentSun = 0;
        this.sunManager = new SunManager(this.arena);
        this.timeManager.registerNewTicker(sunManager);
    }

    public static GameSession getInstance() {
        if (instance == null) {
            System.out.println("Instance is null");
        }
        return instance;
    }

    public static GameSession getInstance(Chapter chapter, Arena arena, List<Plant> chosenPlants, List<Zombie> chosenZombies) {
        if (instance == null) {
            instance = new GameSession(chapter, arena, chosenPlants, chosenZombies);
        }
        return instance;
    }

    public void instantiateCooldowns(List<Plant> chosenPlants) {
        for (Plant plant : chosenPlants) {
            plantsCooldown.put(plant, 0);
        }
    }

    public void spawnZombie(Zombie z, int lane) {

    }

    public void spawnPlant(Plant plant) {

    }

    public void startGame() {
        while (!isGameOver) {
            timeManager.tick();

            for (Plant p : ChosenPlants) p.onTick(timeManager.getCurrentTick());
            for (Zombie z : chosenZombies) z.onTick(timeManager.getCurrentTick());
            for (Projectile proj : activeProjectiles) proj.move();

            checkCollisions();
        }
    }

    public List<Zombie> zombieInRow(int row) {
        List<Zombie> zombies = new ArrayList<>();
        for (Zombie z : chosenZombies) {
            if (z.getRow() == row) {
                zombies.add(z);
            }
        }
        return zombies;
    }

    public void update(int timeAmount) {
        if (this.state != GameState.RUNNING) return;

        for (int i = 0; i < timeAmount; i++) {
            timeManager.tick();
            if (this.currentMode != null)
                this.currentMode.onTick(this, timeManager.getCurrentTick());

            removeDeadEntities();
            checkGameConditions();
            if (this.state == GameState.WON || this.state == GameState.LOST) break;
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

    public void addSun(int amount) {
        this.currentSun += amount;
    }

    private void checkGameConditions() {
        if (this.currentMode == null) return;
        GameState result = this.currentMode.checkResult(this);

        if (result == GameState.LOST) {
            this.state = GameState.LOST;
            this.isGameOver = true;
            this.event = GameEvent.GAME_OVER;
            System.out.println("Zombies ate your brains! GAME OVER.");
        }
        else if (result == GameState.WON) {
            this.state = GameState.WON;
            this.isGameOver = true;
            this.event = GameEvent.LEVEL_COMPLETED;
            System.out.println("You survived! LEVEL COMPLETED.");
        }
    }

    public void checkCollisions() {
    }

    public void addProjectile(Projectile p) {
        activeProjectiles.add(p);
    }

    public void removeZombie(Zombie z) {
        chosenZombies.remove(z);
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public Arena getArena() {
        return arena;
    }

    public Wave getWaveManager() {
        return waveManager;
    }


    public List<Projectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    public List<Zombie> getChosenZombies() {
        return chosenZombies;
    }

    public List<Plant> getChosenPlants() {
        return ChosenPlants;
    }

    public boolean isGameOver() {
        return isGameOver;
    }

    public Chapter getCurrentChapter() {
        return currentChapter;
    }

    public GameEvent getEvent() {
        return event;
    }

    public void setEvent(GameEvent event) {
        this.event = event;
    }

    public boolean isZombieBreached() {
        return zombieBreached;
    }

    public void setZombieBreached(boolean zombieBreached) {
        this.zombieBreached = zombieBreached;
    }

    public int getCurrentSun() {
        return currentSun;
    }

    public void setCurrentSun(int currentSun) {
        this.currentSun = currentSun;
    }
}
