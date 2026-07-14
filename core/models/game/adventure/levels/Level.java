package models.game.adventure.levels;

import models.InGameEntityGenerator;
import models.entities.plants.IPlant;
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


    public void startNextWave() {
        currentWave++;
        boolean isLastWave = (currentWave == waveCount);

        if (currentWave == 1) {
            currentDifficulty = baseWaveDifficulty;
        } else {
            if (isLastWave) {
                currentDifficulty = currentDifficulty * 2;
            } else {
                currentDifficulty = (int) (currentDifficulty * 1.25);
            }
        }

        currentActiveWave = new Wave(currentWave, isLastWave, currentDifficulty);

        if (isLastWave) {
            System.out.println("The final wave has come.");
        } else {
            System.out.println("Wave " + currentWave + " started.");
        }

        spawnWave(currentActiveWave);

        if (isLastWave) {
            allWavesSpawned = true;
        }
    }

    protected void spawnWave(Wave wave) {
        int targetDifficulty = wave.getDifficulty();
        int accumulatedCost = 0;

        List<Zombie> allowedZombies = GameSession.getInstance().getChosenZombies();
        if (allowedZombies.isEmpty()) return;

        java.util.Random random = new java.util.Random();

        while (accumulatedCost < targetDifficulty) {
            Zombie template = allowedZombies.get(random.nextInt(allowedZombies.size()));

            int lane = random.nextInt(5);

            Zombie newZombie = InGameEntityGenerator.getZombieForGame(template.getType(), lane);
            newZombie.setCol(8);

            wave.addZombie(newZombie);
            accumulatedCost += newZombie.getWaveCost();

            GameSession.getInstance().getArena().addZombie(newZombie);

            GameSession.getInstance().getTimeManager().registerNewTicker(newZombie);

            System.out.println("Zombie " + newZombie.getType().name() +
                    " spawned at wave " + wave.getCurrentNumber() +
                    " in lane " + lane +
                    " which costed " + newZombie.getWaveCost() + ".");
        }
    }

    public boolean skipsPlantSelection() {
        return false;
    }

    public boolean skySunFalls() {
        return season != SeasonType.DARK_AGES;
    }

    public boolean isPlantAllowed(IPlant plant) {
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
