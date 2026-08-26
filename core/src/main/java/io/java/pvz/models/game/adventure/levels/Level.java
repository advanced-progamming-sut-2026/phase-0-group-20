package io.java.pvz.models.game.adventure.levels;

import io.java.pvz.models.App;
import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Wave;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.GameState;
import io.java.pvz.models.fields.modifiers.SeasonModifier;
import io.java.pvz.models.game.GameMode;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.LoseCondition;
import io.java.pvz.models.game.WinCondition;
import io.java.pvz.models.game.adventure.Chapter;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.events.DelayedEventTicker;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.utils.DialogueLine;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static io.java.pvz.models.enums.PhysicalConstants.GRID_START_X;
import static io.java.pvz.models.enums.PhysicalConstants.TILE_WIDTH;


public abstract class Level implements GameMode {

    protected final String name;
    protected final int levelNumber;
    protected final SeasonType season;
    protected final SeasonModifier seasonModifier;
    protected final List<WinCondition> winConditions = new ArrayList<>();
    protected final List<LoseCondition> loseConditions = new ArrayList<>();

    protected final int waveCount;
    protected final int baseWaveBudget;
    protected int currentWave = 0;
    private boolean allWavesSpawned = false;
    private Wave currentActiveWave = null;
    private float currentDifficulty;
    protected List<DialogueLine> introDialogue = new ArrayList<>();

    public List<DialogueLine> getIntroDialogue() {
        return introDialogue;
    }

    public void setIntroDialogue(List<DialogueLine> introDialogue) {
        this.introDialogue = introDialogue;
    }

    public void addDialogueLine(String speakerName, String text, String pamPath, String clipName, boolean isLeft) {
        this.introDialogue.add(new DialogueLine(speakerName, text, pamPath, clipName, isLeft));
    }

    protected Level(String name, SeasonType season, int waveCount, int baseWaveBudget, int levelNumber) {
        this.name = name;
        this.season = season;
        this.seasonModifier = Chapter.createModifier(season);
        this.waveCount = waveCount;
        this.baseWaveBudget = (int) (baseWaveBudget * getDifficultyCoefficient());
        this.levelNumber = levelNumber;
        setupDialogues();
    }

    public void onStart(GameSession session) {
        this.currentWave = 0;
        this.allWavesSpawned = false;
        this.currentActiveWave = null;
        this.currentDifficulty = 0;

        if (seasonModifier != null)
            seasonModifier.onCurrentLevelStart();

        onLevelStart(session);
    }

    public void destroyLevelFields() {
    }

    ;

    public abstract void onLevelStart(GameSession session);

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
            System.out.println("fuckkkkk");
            session.getTimeManager().registerNewTicker(
                new DelayedEventTicker(GameEvent.WAVE_STARTED_PLAYTIME, 6f)
            );
            startNextWave(session);
        } else if (currentActiveWave != null && currentActiveWave.is75PercentHpDestroyed()) {
            startNextWave(session);
        }
    }

    public void startNextWave(GameSession session) {
        currentWave++;
        boolean isLastWave = (currentWave == waveCount);

        if (currentWave == 1) {
            currentDifficulty = Math.max(1000, baseWaveBudget);
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
        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.WAVE_STARTED).build();
        GameEventMessenger.getInstance().dispatch(GameEvent.WAVE_STARTED, payload);
        Random random = new Random();
        while (accumulatedCost < targetDifficulty) {
            Zombie template = allowedZombies.get(random.nextInt(allowedZombies.size()));
            int lane = random.nextInt(session.getArena().getRows());

            Zombie newZombie = InGameEntityGenerator.getZombieForGame(template.getType(), lane);
            if (shinyZombie()) {
                newZombie.setShiny(true);
            }

            int waveCount = wave.getCurrentNumber();
            int randomX = random.nextInt(100);

            if (waveCount == 1)
                randomX += (int) (TILE_WIDTH) / 2;

            newZombie.setCol(session.getArena().getCols() - 1);
            if (!newZombie.getName().equalsIgnoreCase("ZombieDarkKing"))
                newZombie.setX(GRID_START_X + TILE_WIDTH * 9 + 300 + randomX * 6);

            wave.addZombie(newZombie);
            accumulatedCost += newZombie.getWaveCost();

            session.getArena().addZombie(newZombie);
            session.getTimeManager().registerNewTicker(newZombie);

            if (seasonModifier != null) {
                seasonModifier.onZombieSpawn(newZombie, session.getArena());
            }

        }
    }

    protected boolean shinyZombie() {
        int chance = new Random().nextInt(100);
        if (chance < 5) return true;
        return false;
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
        int diffLevel = App.getSettings().getDifficulty();
        return 0.4f + (diffLevel * 0.2f);
    }

    public void setupDialogues() {

    }

}
