package models.game.adventure.levels;

import models.InGameEntityGenerator;
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
    private Wave currentActiveWave = null;
    private int currentDifficulty;

    protected Level(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int levelNumber) {
        this.name = name;
        this.season = season;
        this.seasonModifier = Chapter.createModifier(season);
        this.waveCount = waveCount;
        this.baseWaveDifficulty = baseWaveDifficulty;
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

        Wave activeWave = session.getArena().getCurrentActiveWave();

        if (currentWave == 0) {
            startNextWave(session);
        } else if (activeWave != null && activeWave.is75PercentHpDestroyed()) {
            startNextWave(session);
        }
    }

    public void startNextWave(GameSession session) {
        currentWave++;
        boolean isLastWave = (currentWave == waveCount);

        if (currentWave == 1) {
            currentDifficulty = baseWaveDifficulty;
        } else {
            currentDifficulty = isLastWave ? currentDifficulty * 2 : (int) (currentDifficulty * 1.25);
        }

        Wave newWave = new Wave(currentWave, isLastWave, currentDifficulty);
        session.getArena().setCurrentActiveWave(newWave);

        System.out.println(isLastWave ? "The final wave has come." : "Wave " + currentWave + " started.");

        spawnWave(newWave, session);

        if (isLastWave) {
            allWavesSpawned = true;
        }
    }

    protected void spawnWave(Wave wave, GameSession session) {
        int targetDifficulty = wave.getDifficulty();
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

            System.out.println("Zombie " + newZombie.getType().name() +
                    " spawned in lane " + lane + " (Cost: " + newZombie.getWaveCost() + ").");
        }
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

}
