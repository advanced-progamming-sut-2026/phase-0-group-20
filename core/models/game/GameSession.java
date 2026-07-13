package models.game;

import models.entities.PlantFood;
import models.entities.Sun;
import models.entities.SunType;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.enums.GameState;
import models.fields.tiles.Tile;
import models.game.adventure.Chapter;
import models.game.events.GameEvent;
import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameSession {
    private static final int PLANT_COOLDOWN = 50;
    private static GameSession instance;
    private final List<Plant> chosenPlants;
    private final List<Zombie> chosenZombies;
    private final List<Projectile> activeProjectiles = new ArrayList<>();
    private final List<PlantFood> plantFoods = new ArrayList<>();
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
        this.chosenPlants = chosenPlants;// this should come from PlantSelectionMenu Not the arena
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

            for (Plant p : chosenPlants) p.onTick(timeManager.getCurrentTick());
            for (Zombie z : chosenZombies) z.onTick(timeManager.getCurrentTick());
            for (Projectile proj : activeProjectiles) proj.move();

            checkCollisions();
        }
    }

    public void update(int timeAmount) {
        if (this.state != GameState.RUNNING) return;

        for (int i = 0; i < timeAmount; i++) {
            timeManager.tick();
            if (this.currentMode != null)
                this.currentMode.onTick(this, timeManager.getCurrentTick());

            removeDeadEntities();
            checkGameConditions();
            checkCollisions();
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

        activeProjectiles.removeIf(proj -> {
            if (proj.isDestroyed()) {
                timeManager.unregisterTicker(proj);
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
        } else if (result == GameState.WON) {
            this.state = GameState.WON;
            this.isGameOver = true;
            this.event = GameEvent.LEVEL_COMPLETED;
            System.out.println("You survived! LEVEL COMPLETED.");
        }
    }

    public void checkCollisions() {
        List<Plant> activePlants = arena.getActivePlants();
        List<Zombie> activeZombies = arena.getActiveZombies();

        //for projectiles
        for (Projectile proj : activeProjectiles) {
            if (proj.isDestroyed()) continue;
            float projectileHitRadius = 0.25f;
            float zombieHitRadius = 0.25f;
            int topRow = (int) (proj.getY() + projectileHitRadius);
            int bottomRow = (int) (proj.getY() - projectileHitRadius);

            topRow = Math.min(arena.getRows() - 1, Math.max(0, topRow));
            bottomRow = Math.min(arena.getRows() - 1, Math.max(0, bottomRow));

            List<Zombie> nearbyZombies = new ArrayList<>();
            for (int row = bottomRow; row <= topRow; row++) {
                nearbyZombies.addAll(arena.zombieInRow(row));
            }

            for (Zombie z : nearbyZombies) {
                if (z.isDead()) continue;

                double dx = proj.getX() - z.getX();
                double dy = proj.getY() - z.getRow();
                double distanceSquared = (dx * dx) + (dy * dy);

                double combinedRadius = projectileHitRadius + zombieHitRadius;

                if (distanceSquared <= (combinedRadius * combinedRadius)) {
                    proj.onHit(z);
                    if (!proj.isPiercing() || proj.isDestroyed()) {
                        break;
                    }
                }
            }
        }
        // for plants&zombies
        for (Zombie z : chosenZombies) {
            if (z.isDead()) continue;

            int row = z.getRow();
            int targetCol = (int) (z.getX() - 0.2);//mostly for phase 2... If you want You can remove the front threshold

            Tile targetTile = arena.getTile(row, targetCol);
            List<Plant> plantToEat = targetTile.getPlants();
            Plant eatingPlant = null;
            if (!plantToEat.isEmpty()) {
                eatingPlant = plantToEat.get(plantToEat.size() - 1);
            }


            if (eatingPlant != null) {
                if (!z.isAttacking()) {
                    z.setAttacking(true);
                    z.setTile(targetTile);
                    eatingPlant.takeDamage(z.getEatDPS() / 10);
                }
            } else if (z.isAttacking()) {
                z.setAttacking(false);
                z.setTile(null);//plant got plucked.
            }
        }

        for (Sun sun : arena.getActiveSuns()) {
            if (sun.isCollected() && sun.isFalling() && sun.getType() == SunType.RADIOACTIVE_SUN) {
                Tile sunTile = arena.getTile(sun.getX(), sun.getY()); //damaging zombies
                int rightTile = Math.min(sunTile.getCol() + 2, arena.getCols() - 1);
                int leftTile = Math.max(sunTile.getCol() - 2, 0);
                int upTile = Math.min(sunTile.getRow() + 2, arena.getRows() - 1);
                int downTile = Math.max(sunTile.getRow() - 2, 0);
                List<Tile> affectedTiles = new ArrayList<>();
                for (int row = downTile; row <= upTile; row++) {
                    for (int col = leftTile; col <= rightTile; col++) {
                        affectedTiles.add(arena.getTile(row, col));
                    }
                }
                for (Zombie z : arena.getActiveZombies()) {
                    if (z.isDead() || !affectedTiles.contains(z.getTile())) continue;
                    z.takeDamage(150);
                }
                rightTile = Math.min(sunTile.getCol() + 1, arena.getCols() - 1);
                leftTile = Math.max(sunTile.getCol() - 1, 0);
                upTile = Math.min(sunTile.getRow() + 1, arena.getRows() - 1);
                downTile = Math.max(sunTile.getRow() - 1, 0);
                for (int row = downTile; row <= upTile; row++) {
                    for (int col = leftTile; col <= rightTile; col++) {
                        List <Plant> tilePlants = arena.getTile(row, col).getPlants();
                        Plant damagePlant = tilePlants.get(tilePlants.size() - 1);
                        damagePlant.takeDamage(80);
                    }
                }

                sun.setExploded(true);
            }
        }
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
        return chosenPlants;
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

    public void spawnPlantFood(int row, int col) {
        PlantFood pf = new PlantFood(row, col);
        this.plantFoods.add(pf);
        this.getTimeManager().registerNewTicker(pf);
    }

    public boolean collectPlantFoodAt(int row, int col) {
        for (PlantFood pf : plantFoods) {
            if (pf.getRow() == row && pf.getCol() == col && !pf.isCollected() && !pf.isExpired()) {
                pf.collect();
                return true;
            }
        }
        return false;
    }


    public void cleanUpExpiredPlantFoods() {
        plantFoods.removeIf(pf -> pf.isCollected() || pf.isExpired());
    }


    public List<PlantFood> getPlantFoods() {
        return plantFoods;
    }

    public void addPlantFood(PlantFood pf) {
        plantFoods.add(pf);
    }

    public void consumePlantFood() {
        if (plantFoods.isEmpty()) return;
        plantFoods.remove(plantFoods.size() - 1);
    }

    public void setPlantCooldown(Plant plant) {
        plantsCooldown.computeIfPresent(plant, (key, value) -> PLANT_COOLDOWN);
    }

    public HashMap<Plant, Integer> getPlantsCooldown() {
        return plantsCooldown;
    }
}
