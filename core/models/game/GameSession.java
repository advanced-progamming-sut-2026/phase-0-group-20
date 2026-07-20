package models.game;

import models.App;
import models.entities.PlantFood;
import models.entities.Sun;
import models.entities.SunType;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.enums.GameState;
import models.enums.Menu;
import models.enums.PhysicalConstants;
import models.enums.plants.PlantCategory;
import models.fields.Brain;
import models.fields.LawnMower;
import models.fields.tiles.Tile;
import models.game.adventure.Adventure;
import models.game.adventure.Chapter;
import models.game.adventure.levels.Level;
import models.game.events.*;
import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameSession {
    private static final int PLANT_COOLDOWN = 50;
    private static GameSession instance;
    private final List<Plant> chosenPlants;
    private final List<Zombie> chosenZombies;
    private final List<PlantFood> plantFoods = new ArrayList<>();
    private final TimeManager timeManager;
    private final Arena arena;
    private final Chapter currentChapter;
    private boolean isGameOver = false;
    private int currentSun;
    private GameEvent event = GameEvent.GAME_STARTED;
    private GameState state = GameState.RUNNING;
    private SunManager sunManager;
    private HashMap<Plant, Integer> plantsCooldown;
    private GameMode currentMode;
    private boolean zombieBreached = false;
    private ZombieDropListener dropListener;
    private ProgressListener progressListener;

    private GameSession(Chapter chapter, Arena arena, List<Plant> chosenPlants, List<Zombie> chosenZombies) {
        this.currentChapter = chapter;
        this.arena = arena;
        this.timeManager = new TimeManager();
        this.chosenPlants = chosenPlants;// this should come from PlantSelectionMenu Not the arena
        plantsCooldown = new HashMap<>();
        instantiateCooldowns(chosenPlants);
        this.chosenZombies = chosenZombies;// this should come from our level Modifiers or something else
        this.currentSun = 50;
        this.sunManager = new SunManager(this.arena);
        this.timeManager.registerNewTicker(sunManager);
        this.dropListener = new ZombieDropListener();
        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBIE_KILLED, this.dropListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBIE_KILLED_LAWN_MOWER, this.dropListener);

        this.progressListener = new ProgressListener();
        currentMode = chapter.getCurrentLevel();
    }

    public static GameSession getInstance() {
        if (instance == null) {
            notify("Instance is null");
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

    public void resetCooldownsForCategory(PlantCategory category) {
        if (plantsCooldown != null) {
            plantsCooldown.replaceAll((plant, cooldown) ->
                    (plant.getCategory() == category) ? 0 : cooldown
            );
        }
    }

    public void setCooldownForPlant(Plant plant){
        plantsCooldown.computeIfPresent(plant, (key, value) -> PLANT_COOLDOWN);
    }

    public static void startNewGame(List<Plant> inGamePlants) {
        Adventure adventure = models.App.getActiveAdventure();
        Level currentLevel = adventure.getCurrentChapter().getCurrentLevel();

        List<models.entities.zombies.Zombie> inGameZombies = models.InGameEntityGenerator.getZombiesForLevel(
                adventure.getCurrentChapter().getSeasonType(),
                currentLevel.getLevelNumber()
        );

        Arena arena = new models.game.Arena();
        GameSession.destroyInstance();
        GameSession session = GameSession.getInstance(adventure.getCurrentChapter(), arena, inGamePlants, inGameZombies);
        arena.registerLawnMowers();
        models.App.setActiveSession(session);
    }

    public static void destroyInstance() {
        if (instance != null) {
            if (instance.dropListener != null) {
                GameEventMessenger.getInstance().removeListener(GameEvent.ZOMBIE_KILLED, instance.dropListener);
                GameEventMessenger.getInstance().removeListener(GameEvent.ZOMBIE_KILLED_LAWN_MOWER, instance.dropListener);
            }
            if (instance.progressListener != null)
                instance.progressListener.unregisterFromAllEvents();
        }

        instance = null;
    }

    public void update(int timeAmount) {
        if (this.state != GameState.RUNNING) return;

        for (int i = 0; i < timeAmount; i++) {
            timeManager.tick();
            if (currentMode != null) {
                currentMode.engineLoop(this, timeManager.getCurrentTick());
            }

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
                GameEventPayload payload = new GameEventPayload.Builder(GameEvent.PLANT_LOST)
                        .plant(plant)
                        .coordinate(plant.getPlacedTile().getRow(), plant.getPlacedTile().getCol())
                        .arena(arena)
                        .build();
                GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_LOST, payload);
                return true;
            }
            return false;
        });

        getArena().getActiveProjectiles().removeIf(proj -> {
            if (proj.isDestroyed()) {
                timeManager.unregisterTicker(proj);
                return true;
            }
            return false;
        });
    }

    public static void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
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
            GameEventPayload payload = new GameEventPayload.Builder(GameEvent.GAME_OVER)
                    .arena(arena)
                    .build();
            GameEventMessenger.getInstance().dispatch(GameEvent.GAME_OVER, payload);
            notify("Zombies ate your brains! GAME OVER.");
        } else if (result == GameState.WON) {
            this.state = GameState.WON;
            this.isGameOver = true;
            GameEventPayload payload = new GameEventPayload.Builder(GameEvent.LEVEL_COMPLETED)
                    .arena(arena)
                    .build();
            GameEventMessenger.getInstance().dispatch(GameEvent.LEVEL_COMPLETED, payload);
            notify("You survived! LEVEL COMPLETED.");
            App.setActiveMenu(Menu.GAME_MENU);
        }
    }

    public void checkCollisions() {
        List<Plant> activePlants = arena.getActivePlants();
        List<Zombie> activeZombies = arena.getActiveZombies();

        //for projectiles
        for (Projectile proj : getArena().getActiveProjectiles()) {
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
                double dy = proj.getY() - z.getY();
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
        for (Zombie z : activeZombies) {
            if (z.isDead()) continue;

            int row = z.getRow();
            int targetCol = (int) (z.getX() / PhysicalConstants.TILE_UNIT_LENGTH - 0.2);//mostly for phase 2... If you want You can remove the front threshold

            Tile targetTile = arena.getTile(row, targetCol);

//            if (z.isHypnotized()) {
//                Zombie z =
//            }

            if (targetTile != null) {
                List<Plant> plantToEat = targetTile.getPlants();
                Plant eatingPlant = null;
                if (!plantToEat.isEmpty()) {
                    eatingPlant = plantToEat.get(plantToEat.size() - 1);
                }


                if (eatingPlant != null) {
                    if (!z.isAttacking()) {
                        z.setAttacking(true);
                        z.setTile(targetTile);
                        eatingPlant.takeDamage(z.getEatDPS() / TimeManager.TICKS_PER_SECOND);
                    }
                } else if (z.isAttacking()) {
                    z.setAttacking(false);
                    z.setTile(null);//plant got plucked.
                }
            } else if (targetCol < 0) {

                LawnMower lawnMower = arena.getLawnMowers()[row];

                if (lawnMower != null && !lawnMower.isActivate()) continue; //lawn mower will handle by itself

                else {
                    Brain targetBrain = arena.getBrainInRow(row);

                    if (targetBrain != null && !targetBrain.isEaten()) {
                        if (!z.isAttacking()) {
                            z.setAttacking(true);
                            z.setTile(null);
                        }
                        targetBrain.takeDamage(z.getEatDPS() / 10);
                    } else {
                        if (z.isAttacking()) z.setAttacking(false);
                        if (z.getX() < -PhysicalConstants.TILE_UNIT_LENGTH) this.zombieBreached = true;
                    }
                }
            }

        }

        for (Sun sun : arena.getActiveSuns()) {
            if (sun.isCollected() && sun.isFalling() && sun.getType() == SunType.RADIOACTIVE_SUN) {
                Tile sunTile = arena.getTile(sun.getCol(), sun.getRow()); //damaging zombies
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
                    boolean killed = z.takeDamage(150);
                    if (killed) {
                        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.ZOMBIE_KILLED)
                                .seasonType(getCurrentChapter().getSeasonType())
                                .coordinate(z.getRow(), z.getCol())
                                .build();
                        GameEventMessenger.getInstance().dispatch(GameEvent.ZOMBIE_KILLED, payload);
                    }
                }
                rightTile = Math.min(sunTile.getCol() + 1, arena.getCols() - 1);
                leftTile = Math.max(sunTile.getCol() - 1, 0);
                upTile = Math.min(sunTile.getRow() + 1, arena.getRows() - 1);
                downTile = Math.max(sunTile.getRow() - 1, 0);
                for (int row = downTile; row <= upTile; row++) {
                    for (int col = leftTile; col <= rightTile; col++) {
                        List<Plant> tilePlants = arena.getTile(row, col).getPlants();
                        Plant damagePlant = tilePlants.get(tilePlants.size() - 1);
                        damagePlant.takeDamage(80);
                    }
                }

                sun.setExploded(true);
            }
        }
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

    public void useSun(int amount) {
        this.currentSun -= amount;
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

    public GameMode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(GameMode currentMode) {
        this.currentMode = currentMode;
    }
}
