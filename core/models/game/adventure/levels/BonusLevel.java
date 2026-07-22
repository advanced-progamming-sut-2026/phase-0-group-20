package models.game.adventure.levels;

import models.App;
import models.InGameEntityGenerator;
import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.game.GameSession;

import models.game.adventure.SeasonType;
import models.game.adventure.levels.conditions.NormalLoseCondition;
import models.game.adventure.levels.conditions.NormalWinCondition;
import models.game.events.*;
import models.users.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class BonusLevel extends Level implements GameEventListener {

    private boolean isDailyChallenge;
    private ScoreManager scoreManager;

    public BonusLevel(String name, SeasonType season, int waveCount, int baseWaveBudget, int levelNumber, boolean isDailyChallenge) {

        super(name, season, waveCount, baseWaveBudget, levelNumber);
        this.isDailyChallenge = isDailyChallenge;

        this.addLoseCondition(new NormalLoseCondition());
        this.addWinCondition(new NormalWinCondition());
    }

    @Override
    public void onStart(GameSession session) {
        this.scoreManager = new ScoreManager();
        session.getTimeManager().registerNewTicker(this.scoreManager);

        GameEventMessenger.getInstance().addListener(GameEvent.LEVEL_COMPLETED, this);
        GameEventMessenger.getInstance().addListener(GameEvent.GAME_OVER, this);

        notify("Scoring Mode Started! Rack up as many Mewpoints as you can.");
        if (isDailyChallenge) {
            notify("This is a Daily Challenge! Everyone faces the same zombies today.");
        }
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.LEVEL_COMPLETED || event == GameEvent.GAME_OVER) {
            User user = App.getActiveUser();
            int finalScore = scoreManager.getTotalMewpoints();

            String message = "Bonus Level Ended! Your Score: " + finalScore + " Mewpoints.";

            if (user != null) {
                if (finalScore > user.getHighestBonusScore()) {
                    user.setHighestBonusScore(finalScore);
                    message += " NEW HIGH SCORE!";
                } else {
                    message += " (Best: " + user.getHighestBonusScore() + ")";
                }
            }

            notify(message);

            scoreManager.unregister();
            GameEventMessenger.getInstance().removeListener(GameEvent.LEVEL_COMPLETED, this);
            GameEventMessenger.getInstance().removeListener(GameEvent.GAME_OVER, this);
        }
    }

    @Override
    protected void spawnWave(Wave wave, GameSession session) {
        float targetDifficulty = wave.getDifficulty();
        int accumulatedCost = 0;

        Random waveRandom;
        List<Zombie> availableZombies;

        if (isDailyChallenge) {
            long waveSeed = (LocalDate.now().toEpochDay() * 1000L) + wave.getCurrentNumber();
            waveRandom = new Random(waveSeed);

            availableZombies = InGameEntityGenerator.getZombiesForDailyChallenge();
        } else {
            waveRandom = new Random();
            availableZombies = session.getChosenZombies();
        }

        if (availableZombies == null || availableZombies.isEmpty()) return;

        while (accumulatedCost < targetDifficulty) {
            Zombie template = availableZombies.get(waveRandom.nextInt(availableZombies.size()));

            int lane = waveRandom.nextInt(session.getArena().getRows());

            Zombie newZombie = InGameEntityGenerator.getZombieForGame(template.getType(), lane);
            newZombie.setCol(session.getArena().getCols() - 1);

            wave.addZombie(newZombie);
            accumulatedCost += newZombie.getWaveCost();

            session.getArena().addZombie(newZombie);
            session.getTimeManager().registerNewTicker(newZombie);

            if (seasonModifier != null) {
                seasonModifier.onZombieSpawn(newZombie, session.getArena());
            }

            notify("Zombie " + newZombie.getType().name() +
                    " spawned in lane " + (lane + 1) + ".");
        }
    }

    public ScoreManager getScoreManager() {
        return scoreManager;
    }

    public boolean isDailyChallenge() {
        return isDailyChallenge;
    }

    public void setDailyChallenge(boolean dailyChallenge) {
        this.isDailyChallenge = dailyChallenge;
    }
}