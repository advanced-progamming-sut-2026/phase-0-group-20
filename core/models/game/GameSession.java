package models.game;

import models.App;
import models.InGameEntityGenerator;
import models.entities.PlantFood;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.GameState;
import models.enums.plants.PlantCategory;
import models.fields.modifiers.SeasonModifier;
import models.game.adventure.Adventure;
import models.game.adventure.Chapter;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.BonusLevel;
import models.game.adventure.levels.Level;
import models.game.events.*;
import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameSession {
    private static final int PLANT_COOLDOWN = 50;
    private static GameSession instance;
    // for mew points
    private static BonusLevel pendingBonusLevel = null;
    // for minigame
    private static Level minigameLevel = null;
    //for all level
    private static Level pendingLevel = null;
    private static Chapter pendingChapter = null;

    private final List<Plant> chosenPlants;
    private final List<Zombie> chosenZombies;
    private final List<PlantFood> plantFoods = new ArrayList<>();
    private final TimeManager timeManager;
    private final Arena arena;
    private final Chapter currentChapter;
    private final CollisionManager collisionManager;
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
        this.chosenPlants = chosenPlants;
        plantsCooldown = new HashMap<>();
        if (chosenPlants != null) instantiateCooldowns(chosenPlants);
        this.chosenZombies = chosenZombies;
        this.currentSun = 50;

        this.sunManager = new SunManager(this.arena);
        this.timeManager.registerNewTicker(sunManager);

        this.collisionManager = new CollisionManager(this);

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

    public static GameSession getInstance(Chapter chapter, Arena arena,
                                          List<Plant> chosenPlants, List<Zombie> chosenZombies) {
        if (instance == null) {
            instance = new GameSession(chapter, arena, chosenPlants, chosenZombies);
        }
        return instance;
    }

    public static void startNewGame(List<Plant> inGamePlants) {
        Adventure adventure = App.getActiveAdventure();
        Level currentLevel = pendingLevel;

        List<Zombie> inGameZombies = InGameEntityGenerator.getZombiesForLevel(
                adventure.getCurrentChapter().getSeasonType(),
                currentLevel.getLevelNumber()
        );

        Arena arena = new Arena();
        GameSession.destroyInstance();
        GameSession session = GameSession.getInstance(pendingChapter,
                arena, inGamePlants, inGameZombies);
        arena.registerLawnMowers();
        App.setActiveSession(session);
        App.getActiveUser().addZombiesToUnlock(inGameZombies);
        for (int r = 0; r < arena.getRows(); r++)
            for (int c = 0; c < arena.getCols(); c++)
                session.getTimeManager().registerNewTicker(arena.getTile(r, c));
        currentLevel.onStart(session);
        pendingChapter = null;
        pendingLevel = null;
    }

    public static void startMiniGame(Level minigameLevel, List<Plant> inGamePlants) {
        Arena arena = new Arena();
        GameSession.destroyInstance();

        Chapter fakeChapter = new Chapter(SeasonType.MINI_GAME);

        List<Zombie> inGameZombies = InGameEntityGenerator.getZombiesForLevel(SeasonType.MINI_GAME, minigameLevel.getLevelNumber());

        GameSession session = GameSession.getInstance(fakeChapter, arena, inGamePlants, inGameZombies);

        session.setCurrentMode(minigameLevel);

        arena.registerLawnMowers();
        App.setActiveSession(session);

        for (int r = 0; r < arena.getRows(); r++)
            for (int c = 0; c < arena.getCols(); c++)
                session.getTimeManager().registerNewTicker(arena.getTile(r, c));

        minigameLevel.onStart(session);
    }

    public static void startScoringGame(BonusLevel bonusLevel, List<Plant> inGamePlants) {
        Arena arena = new Arena();
        GameSession.destroyInstance();

        Adventure adventure = App.getActiveAdventure();
        Chapter currentChapter = adventure.getCurrentChapter();

        List<Zombie> inGameZombies;
        if (bonusLevel.isDailyChallenge())
            inGameZombies = InGameEntityGenerator.getZombiesForDailyChallenge();
        else
            inGameZombies = InGameEntityGenerator
                    .getZombiesForLevel(bonusLevel.getSeason(), bonusLevel.getLevelNumber());

        GameSession session = GameSession.getInstance(currentChapter, arena, inGamePlants, inGameZombies);

        session.setCurrentMode(bonusLevel);
        bonusLevel.onStart(session);
        arena.registerLawnMowers();
        App.setActiveSession(session);

        for (int r = 0; r < arena.getRows(); r++)
            for (int c = 0; c < arena.getCols(); c++)
                session.getTimeManager().registerNewTicker(arena.getTile(r, c));

    }

    public static void destroyInstance() {
        if (instance != null) {
            if (instance.dropListener != null) {
                GameEventMessenger.getInstance().removeListener(GameEvent.ZOMBIE_KILLED, instance.dropListener);
                GameEventMessenger.getInstance().removeListener(
                        GameEvent.ZOMBIE_KILLED_LAWN_MOWER,
                        instance.dropListener);
            }
            if (instance.progressListener != null)
                instance.progressListener.unregisterFromAllEvents();
        }

        instance = null;
    }

    public static void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }

    public static BonusLevel getPendingBonusLevel() {
        return pendingBonusLevel;
    }

    public static void setPendingBonusLevel(BonusLevel level) {
        pendingBonusLevel = level;
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

    public void setCooldownForPlant(Plant plant) {
        plantsCooldown.computeIfPresent(plant, (key, value) -> PLANT_COOLDOWN);
    }

    public void update(int timeAmount) {
        if (this.state != GameState.RUNNING) return;

        for (int i = 0; i < timeAmount; i++) {
            timeManager.tick();
            if (currentMode != null)
                currentMode.engineLoop(this, timeManager.getCurrentTick());

            SeasonModifier currentModifier = currentChapter.getModifier();
            if (currentModifier != null)
                currentModifier.updateEnvironment(timeManager.getCurrentTick(), arena);

            removeDeadEntities();
            checkGameConditions();
            collisionManager.checkAllCollisions();

            if (this.state == GameState.WON || this.state == GameState.LOST) {
                isGameOver = true;
                break;
            }
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

    public void setCurrentSun(int currentSun) {
        this.currentSun = currentSun;
    }

    public void useSun(int amount) {
        this.currentSun -= amount;
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
        plantFoods.removeLast();
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

    public static Level getMinigameLevel() {
        return minigameLevel;
    }

    public static void setMinigameLevel(Level minigameLevel) {
        GameSession.minigameLevel = minigameLevel;
    }


    public static void setPendingLevel(Level pendingLevel) {
        GameSession.pendingLevel = pendingLevel;
    }

    public static Chapter getPendingChapter() {
        return pendingChapter;
    }

    public static void setPendingChapter(Chapter pendingChapter) {
        GameSession.pendingChapter = pendingChapter;
    }
}