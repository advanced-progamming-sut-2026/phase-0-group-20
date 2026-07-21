package models.game.adventure.levels;

import models.InGameEntityGenerator;
import models.Settings;
import models.entities.plants.Plant;
import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.enums.GameState;
import models.fields.modifiers.SeasonModifier;
import models.game.GameMode;
import models.game.GameSession;
import models.game.LoseCondition;
import models.game.WinCondition;
import models.game.adventure.Chapter;
import models.game.adventure.SeasonType;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public abstract class Level implements GameMode {

    protected final String name;
    protected final int levelNumber;
    protected final SeasonType season;
    protected final SeasonModifier seasonModifier;
    protected final List<WinCondition> winConditions = new ArrayList<>();
    protected final List<LoseCondition> loseConditions = new ArrayList<>();

    protected final int waveCount;
    protected final int baseWaveDifficulty;
    protected int currentWave = 0;
    private boolean allWavesSpawned = false;
    private Wave currentActiveWave = null; //seems useless
    private float currentDifficulty;

    protected Level(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int levelNumber) {
        this.name = name;
        this.season = season;
        this.seasonModifier = Chapter.createModifier(season);
        this.waveCount = waveCount;
        this.baseWaveDifficulty = (int) (baseWaveDifficulty * getDifficultyCoefficient());
        this.levelNumber = levelNumber;
    }

    public abstract void onStart(GameSession session);

    public GameState checkResult(GameSession session) {
        for (LoseCondition lose : loseConditions)
            if (lose.isLost(session)) return GameState.LOST;

        for (WinCondition win : winConditions)
            if (win.isWon(session)) return GameState.WON;

        return GameState.RUNNING;
    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {
        if (allWavesSpawned) return;

        currentActiveWave = session.getArena().getCurrentActiveWave();

        if (currentWave == 0) {
            startNextWave(session);
        } else if (currentActiveWave != null && currentActiveWave.is75PercentHpDestroyed()) {
            startNextWave(session);
        }
    }

    public void startNextWave(GameSession session) {
        currentWave++;
        boolean isLastWave = (currentWave == waveCount);

        if (currentWave == 1) {
            currentDifficulty = Math.max(1000, baseWaveDifficulty);
        } else {
            int increment = Math.max(500, (int) (currentDifficulty * 0.25));
            currentDifficulty += increment;
        }

        Wave newWave = new Wave(currentWave, isLastWave, currentDifficulty);
        session.getArena().setCurrentActiveWave(newWave);

        if (seasonModifier != null)
            seasonModifier.onWaveStart(newWave);

        notify(isLastWave ? "The final wave has come." : "Wave " + currentWave + " started.");

        spawnWave(newWave, session);

        if (isLastWave) {
            allWavesSpawned = true;
        }
    }

    protected void spawnWave(Wave wave, GameSession session) {
        float targetDifficulty = wave.getDifficulty();
        int accumulatedCost = 0;

        List<Zombie> allowedZombies = session.getChosenZombies();
        if (allowedZombies.isEmpty()) return;

        Random random = new Random();
        while (accumulatedCost < targetDifficulty) {
            Zombie template = allowedZombies.get(random.nextInt(allowedZombies.size()));
            int lane = random.nextInt(session.getArena().getRows());

            Zombie newZombie = InGameEntityGenerator.getZombieForGame(template.getType(), lane);
            newZombie.setCol(session.getArena().getCols() - 1); // better for the later arrangements

            wave.addZombie(newZombie);
            accumulatedCost += newZombie.getWaveCost();

            session.getArena().addZombie(newZombie);
            session.getTimeManager().registerNewTicker(newZombie);

            if (seasonModifier != null) {
                seasonModifier.onZombieSpawn(newZombie, session.getArena());
            }

            notify("Zombie " + newZombie.getType().name() +
                    " spawned in lane " + (lane + 1) + " (Cost: " + newZombie.getWaveCost() + ").");
        }
    }

    public void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }

    public boolean skipsPlantSelection() {
        return false;
    }

    public boolean skySunFalls() {
        return season != SeasonType.DARK_AGES;
    }

    public boolean isPlantAllowed(Plant plant) {
        return true;
    }

    public boolean ignoresRecharge() {
        return false;
    }

    public int getInitialSun() {
        return 50;
    }

    public int getPlantSlotCount() {
        return 8;
    }

    public void addWinCondition(WinCondition condition) {
        winConditions.add(condition);
    }

    public void addLoseCondition(LoseCondition condition) {
        loseConditions.add(condition);
    }

    public String getName() {
        return name;
    }

    public SeasonType getSeason() {
        return season;
    }

    public SeasonModifier getSeasonModifier() {
        return seasonModifier;
    }

    public int getWaveCount() {
        return waveCount;
    }

    public int getCurrentWave() {
        return currentWave;
    }

    public boolean allWavesSpawned() {
        return allWavesSpawned;
    }

    public int getLevelNumber() {
        return levelNumber;
    }

    public float getDifficultyCoefficient() {
        int diffLevel = Settings.getInstance().getDifficulty();
        return 0.4f + (diffLevel * 0.2f);
    }

}
