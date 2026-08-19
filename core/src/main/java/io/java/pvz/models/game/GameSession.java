package io.java.pvz.models.game;

import io.java.pvz.models.App;
import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.PlantFood;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.GameState;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.models.fields.modifiers.SeasonModifier;
import io.java.pvz.models.game.adventure.Adventure;
import io.java.pvz.models.game.adventure.Chapter;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.BonusLevel;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.events.*;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.models.users.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameSession {

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
    private HashMap<Plant, Float> plantsCooldown;
    private GameMode currentMode;
    private boolean zombieBreached = false;
    private ZombieDropListener dropListener;
    private ProgressListener progressListener;
    private int imitaterTargetId = -1;

    private float speedMultiplier = 1.0f;

    private GameSession(Chapter chapter, Level currentLevel,
                        Arena arena, List<Plant> chosenPlants, List<Zombie> chosenZombies) {
        this.currentChapter = chapter;
        this.arena = arena;
        this.timeManager = new TimeManager();
        this.chosenPlants = chosenPlants;
        plantsCooldown = new HashMap<>();
        if (chosenPlants != null) instantiateCooldowns(chosenPlants);
        this.chosenZombies = chosenZombies;

        App.getActiveUser().addZombiesToUnlock(this.chosenZombies);


        this.currentSun = currentLevel.getInitialSun();

        if (currentLevel.skySunFalls())
            this.sunManager = new SunManager(this.arena);
        this.timeManager.registerNewTicker(sunManager);

        this.collisionManager = new CollisionManager(this);

        this.dropListener = new ZombieDropListener();
        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBIE_KILLED, this.dropListener);
        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBIE_KILLED_LAWN_MOWER, this.dropListener);

        this.progressListener = new ProgressListener();
        this.currentMode = currentLevel;
        playTheme(currentMode);
    }

    public static GameSession getInstance() {
        return instance;
    }

    public static void bindInstance(GameSession session) {
        instance = session;
    }

    public static GameSession getInstance(Chapter chapter, Level currentLevel, Arena arena,
                                          List<Plant> chosenPlants, List<Zombie> chosenZombies) {
        if (instance == null) {
            instance = new GameSession(chapter, currentLevel, arena, chosenPlants, chosenZombies);
        }
        return instance;
    }

    public static void startNewGame(List<Plant> inGamePlants) {
        Level currentLevel = pendingLevel;

        Arena arena = new Arena();

        List<Zombie> inGameZombies = InGameEntityGenerator.getZombiesForLevel(
            pendingChapter.getSeasonType(),
            currentLevel.getLevelNumber()
        );

        GameSession.destroyInstance();
        GameSession session = GameSession.getInstance(pendingChapter, currentLevel,
            arena, inGamePlants, inGameZombies);

        arena.registerLawnMowers();
        App.setActiveSession(session);

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

        if (inGamePlants == null || inGamePlants.isEmpty()) {
            User activeUser = App.getActiveUser();
            if (activeUser != null && activeUser.getUnlockedPlants() != null)
                inGamePlants = new ArrayList<>(activeUser.getUnlockedPlants());
            else
                inGamePlants = new ArrayList<>();
        }

        Chapter fakeChapter = new Chapter(SeasonType.MINI_GAME);

        List<Zombie> inGameZombies =
            InGameEntityGenerator.getZombiesForLevel(SeasonType.MINI_GAME, minigameLevel.getLevelNumber());

        GameSession session = GameSession.getInstance(fakeChapter, minigameLevel,
            arena, inGamePlants, inGameZombies);

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
        inGameZombies = InGameEntityGenerator.getZombiesForDailyChallenge(bonusLevel);


        GameSession session = GameSession.getInstance(currentChapter, bonusLevel,
            arena, inGamePlants, inGameZombies);

        session.setCurrentMode(bonusLevel);
        bonusLevel.onStart(session);
        arena.registerLawnMowers();
        App.setActiveSession(session);

        for (int r = 0; r < arena.getRows(); r++)
            for (int c = 0; c < arena.getCols(); c++)
                session.getTimeManager().registerNewTicker(arena.getTile(r, c));

    }

    public static void destroyInstance() {
        App.getActiveUser().getQuestManager().resetOneMissionQuests();
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
        App.getActiveUser().setPlantFoodCount(0);

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
        pendingLevel = null;
        pendingChapter = null;
        minigameLevel = null;
    }

    public void instantiateCooldowns(List<Plant> chosenPlants) {
        plantsCooldown.clear();
        for (Plant plant : chosenPlants) {
            plantsCooldown.put(plant, 0f);
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
        plantsCooldown.computeIfPresent(plant, (key, value) -> plant.getRecharge() * TimeManager.TICKS_PER_SECOND);
    }

    public void update(int timeAmount) {
        if (this.state != GameState.RUNNING) return;
        plantsCooldown.replaceAll((plant, currentCooldown) -> Math.max(0, currentCooldown - timeAmount));
        for (int i = 0; i < timeAmount; i++) {
            timeManager.tick();
            if (currentMode != null)
                currentMode.engineLoop(this, timeManager.getCurrentTick());

            SeasonModifier currentModifier = null;
            if (currentMode instanceof Level levelMode) {
                currentModifier = levelMode.getSeasonModifier();
            } else if (currentChapter != null) {
                currentModifier = currentChapter.getModifier();
            }

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
            if (plant.isDead()) {
                timeManager.unregisterTicker(plant);

                if (plant.getPlacedTile() != null && plant.getPlacedTile().getPlants() != null)
                    plant.getPlacedTile().getPlants().remove(plant);

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


    public HashMap<Plant, Float> getPlantsCooldown() {
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

    public static Level getPendingLevel() {
        return pendingLevel;
    }

    public static void setMinigameLevel(Level minigameLevel) {
        GameSession.minigameLevel = minigameLevel;
        GameSession.pendingLevel = null;
        GameSession.pendingChapter = null;
        GameSession.pendingBonusLevel = null;
    }

    public static void setPendingLevel(Level pendingLevel) {
        GameSession.pendingLevel = pendingLevel;
        GameSession.minigameLevel = null;
        GameSession.pendingBonusLevel = null;
    }

    public static Chapter getPendingChapter() {
        return pendingChapter;
    }

    public static void setPendingChapter(Chapter pendingChapter) {
        GameSession.pendingChapter = pendingChapter;
        GameSession.minigameLevel = null;
        GameSession.pendingBonusLevel = null;
    }

    public void setImitaterTargetId(int id) {
        this.imitaterTargetId = id;
    }

    public int getImitaterTargetId() {
        return imitaterTargetId;
    }

    public GameState getState() {
        return state;
    }

    public void setSpeedMultiplier(float speedMultiplier) {
        this.speedMultiplier = speedMultiplier;
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    public void pauseGame() {
        if (this.state == GameState.RUNNING) {
            this.state = GameState.PAUSED;
        }
    }

    public void resumeGame() {
        if (this.state == GameState.PAUSED) {
            this.state = GameState.RUNNING;
        }
    }

    private void playTheme(GameMode gameMode) {
        if(!(gameMode instanceof Level level) )return;
        SeasonType seasonType = level.getSeason();
        switch (seasonType) {
            case DARK_AGES ->
                GameEventMessenger.getInstance().dispatch(GameEvent.ENTERED_DARK_AGES,
                    new GameEventPayload.Builder(GameEvent.ENTERED_DARK_AGES).build());
            case FROZEN_CAVES ->
                GameEventMessenger.getInstance().dispatch(GameEvent.ENTERED_FROZEN_CAVES,
                    new GameEventPayload.Builder(GameEvent.ENTERED_FROZEN_CAVES).build());
            case ANCIENT_EGYPT ,MINI_GAME->
                GameEventMessenger.getInstance().dispatch(GameEvent.ENTERED_EGYPT,
                    new GameEventPayload.Builder(GameEvent.ENTERED_EGYPT).build());
            case BIG_WAVE_BEACH ->
                GameEventMessenger.getInstance().dispatch(GameEvent.ENTERED_BIG_WAVE_BEACH,
                    new GameEventPayload.Builder(GameEvent.ENTERED_BIG_WAVE_BEACH).build());
        }
    }
}
