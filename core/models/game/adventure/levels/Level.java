package models.game.adventure.levels;

import models.entities.plants.IPlant;
import models.entities.zombies.Zombie;
import models.enums.GameState;
import models.fields.modifiers.SeasonModifier;
import models.game.GameSession;
import models.game.LoseCondition;
import models.game.WinCondition;
import models.game.adventure.Season;
import models.game.adventure.SeasonType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A single playable stage of the adventure. Like plants and zombies, a level is
 * one concrete runtime object whose differences come from composition:
 * a season (environment rules via SeasonModifier), a wave plan (count + base
 * difficulty) and a list of win/lose conditions checked every tick.
 *
 * Lifecycle (driven by GameSession):
 *   onStart(session)  - once, when the level is entered; registers conditions
 *                       that depend on the start tick and applies restrictions.
 *   onTick(session,t) - every tick; default runs the wave schedule.
 *   checkResult(session) - every tick after entities update; lose conditions
 *                       win over win conditions.
 */
public abstract class Level {

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
        this.seasonModifier = Season.createModifier(season);
        this.waveCount = waveCount;
        this.baseWaveDifficulty = baseWaveDifficulty;
    }

    public abstract void onStart(GameSession session);

    /** Default per-tick behavior: advance the wave schedule. Subclasses that
     *  add their own tick logic should still call super.onTick(...). */
    public void onTick(GameSession session, int currentTick) {
        updateWaves(session, currentTick);
    }

    /** Lose conditions are checked first: dying and winning on the same tick is a loss. */
    public GameState checkResult(GameSession session) {
        for (LoseCondition lose : loseConditions) {
            if (lose.isLost(session)) return GameState.LOST;
        }
        for (WinCondition win : winConditions) {
            if (win.isWon(session)) return GameState.WON;
        }
        return GameState.RUNNING;
    }

    // ------------------------------------------------------------------
    // Wave schedule
    // ------------------------------------------------------------------

    /**
     * Difficulty per the doc: each wave is 25% harder than the previous one,
     * and the last ("flag") wave is a super-wave with double the difficulty
     * of the wave before it.
     */
    public int getWaveDifficulty(int waveNumber) {
        if (waveCount > 1 && waveNumber >= waveCount) {
            return (int) Math.round(2 * baseWaveDifficulty * Math.pow(1.25, waveCount - 2));
        }
        return (int) Math.round(baseWaveDifficulty * Math.pow(1.25, waveNumber - 1));
    }

    protected void updateWaves(GameSession session, int currentTick) {
        if (allWavesSpawned) return;
        if (currentWave == 0 || shouldStartNextWave(session)) {
            startNextWave(session, currentTick);
        }
    }

    /** The doc: the next wave starts once 75% of the previous wave's total zombie HP is gone. */
    protected boolean shouldStartNextWave(GameSession session) {
        if (currentWaveInitialHp <= 0) return true;
        int aliveHp = 0;
        for (Zombie z : session.getActiveZombies()) {
            if (!z.isDead()) aliveHp += z.getHealth();
        }
        return aliveHp <= currentWaveInitialHp * 0.25;
    }

    protected void startNextWave(GameSession session, int currentTick) {
        currentWave++;
        if (currentWave >= waveCount) {
            System.out.println("The final wave has come.");
        } else {
            System.out.println("Wave " + currentWave + " started.");
        }

        spawnWave(session, currentWave, getWaveDifficulty(currentWave));
        if (seasonModifier != null) seasonModifier.onWaveStart(null); // pass the Wave object once the wave system carries one

        currentWaveInitialHp = 0;
        for (Zombie z : session.getActiveZombies()) {
            if (!z.isDead()) currentWaveInitialHp += z.getHealth();
        }

        if (currentWave >= waveCount) allWavesSpawned = true;
    }

    /**
     * Integration hook: pick random zombies via ZombieFactory until their total
     * waveCost equals {@code difficulty}, drop each in a random lane, and print
     * "Zombie <type> spawned at wave <n> in lane <m> which cost <waveCost>."
     * Not wired yet - spawning needs the zombie catalog on the session.
     */
    protected void spawnWave(GameSession session, int waveNumber, int difficulty) {
        System.out.println("[wave hook] spawn zombies worth " + difficulty + " wave points for wave " + waveNumber);
    }

    // ------------------------------------------------------------------
    // Restriction hooks - special levels override the ones they change
    // ------------------------------------------------------------------

    /** Conveyor-belt levels enter the game directly, with no plant-selection screen. */
    public boolean skipsPlantSelection() { return false; }

    /** False for night levels (Dark Ages season, Night Ops, Plant What You Get). */
    public boolean skySunFalls() { return season != SeasonType.DARK_AGES; }

    /** Whether the player may pick this plant in the selection menu for this level. */
    public boolean isPlantAllowed(IPlant plant) { return true; }

    /** Plant recharge/cooldown is ignored while this is true (Plant What You Get's build phase). */
    public boolean ignoresRecharge() { return false; }

    public int getInitialSun() { return 50; }

    /** Number of plant-selection slots; the doc's default is 8. */
    public int getPlantSlotCount() { return 8; }

    // ------------------------------------------------------------------

    public void addWinCondition(WinCondition condition) { winConditions.add(condition); }
    public void addLoseCondition(LoseCondition condition) { loseConditions.add(condition); }

    public String getName() { return name; }
    public SeasonType getSeason() { return season; }
    public SeasonModifier getSeasonModifier() { return seasonModifier; }
    public int getWaveCount() { return waveCount; }
    public int getCurrentWave() { return currentWave; }
    public boolean allWavesSpawned() { return allWavesSpawned; }
    public List<WinCondition> getWinConditions() { return Collections.unmodifiableList(winConditions); }
    public List<LoseCondition> getLoseConditions() { return Collections.unmodifiableList(loseConditions); }
}
