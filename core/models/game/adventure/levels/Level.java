package models.game.adventure.levels;

import models.entities.plants.IPlant;
import models.entities.zombies.Zombie;
import models.enums.GameState;
import models.fields.modifiers.SeasonModifier;
import models.game.*;
import models.game.adventure.SeasonType;

import java.util.ArrayList;
import java.util.List;


public abstract class Level implements GameMode {

    protected final String name;
    protected final SeasonType season;
    protected final SeasonModifier seasonModifier;
    protected final List<WinCondition> winConditions = new ArrayList<>();
    protected final List<LoseCondition> loseConditions = new ArrayList<>();

    protected final int waveCount;
    protected final int baseWaveDifficulty;

    protected int currentWave = 0;
    private boolean allWavesSpawned = false;
    private int currentWaveInitialHp = 0;

    protected Level(String name, SeasonType season, int waveCount, int baseWaveDifficulty) {
        this.name = name;
        this.season = season;
        this.seasonModifier = Chapter.createModifier(season);
        this.waveCount = waveCount;
        this.baseWaveDifficulty = baseWaveDifficulty;
    }

    public abstract void onStart(GameSession session);


    public void onTick(GameSession session, int currentTick) {
        updateWaves(session, currentTick);
    }


    public GameState checkResult(GameSession session) {
        for (LoseCondition lose : loseConditions)
            if (lose.isLost(session)) return GameState.LOST;

        for (WinCondition win : winConditions)
            if (win.isWon(session)) return GameState.WON;

        return GameState.RUNNING;
    }


    public int getWaveDifficulty(int waveNumber) {
        if (waveCount > 1 && waveNumber >= waveCount)
            return (int) Math.round(2 * baseWaveDifficulty * Math.pow(1.25, waveCount - 2));

        return (int) Math.round(baseWaveDifficulty * Math.pow(1.25, waveNumber - 1));
    }

    protected void updateWaves(GameSession session, int currentTick) {
        if (allWavesSpawned) return;
        if (currentWave == 0 || shouldStartNextWave(session))
            startNextWave(session, currentTick);
    }

    boolean shouldStartNextWave(GameSession session) {
        if (currentWaveInitialHp <= 0) return true;
        int aliveHp = 0;
        for (Zombie zombie : session.getChosenZombies())
            if (!zombie.isDead()) aliveHp += zombie.getHealth();

        return aliveHp <= currentWaveInitialHp * 0.25;
    }

    protected void startNextWave(GameSession session, int currentTick) {
        currentWave++;
        if (currentWave >= waveCount) // methods will be return Result but just print for now
            System.out.println("The final wave has come.");
        else
            System.out.println("Wave " + currentWave + " started.");

        spawnWave(session, currentWave, getWaveDifficulty(currentWave));
        if (seasonModifier != null) seasonModifier.onWaveStart(null);

        currentWaveInitialHp = 0;
        for (Zombie z : session.getChosenZombies())
            if (!z.isDead()) currentWaveInitialHp += z.getHealth();

        if (currentWave >= waveCount) allWavesSpawned = true;
    }

    protected void spawnWave(GameSession session, int waveNumber, int difficulty) {
        System.out.println("[wave hook] spawn zombies worth " + difficulty + " wave points for wave " + waveNumber);
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

}
