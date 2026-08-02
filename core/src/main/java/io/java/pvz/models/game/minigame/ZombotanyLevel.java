package io.java.pvz.models.game.minigame;

import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.zombies.Wave;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.adventure.levels.conditions.NormalLoseCondition;
import io.java.pvz.models.game.adventure.levels.conditions.NormalWinCondition;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombotanyLevel extends Level implements IMinigame {

    protected ZombotanyLevel(String name, SeasonType season, int waveCount, int baseWaveBudget, int levelNumber) {
        super(name, season, waveCount, baseWaveBudget, levelNumber);
        this.addLoseCondition(new NormalLoseCondition());
        this.addWinCondition(new NormalWinCondition());
    }

    @Override
    public void onLevelStart(GameSession session) {

    }

    @Override
    protected void spawnWave(Wave wave, GameSession session) {
        float targetDifficulty = wave.getDifficulty();
        int accumulatedCost = 0;

        List<Zombie> zombotanyTypes = new ArrayList<>();

        zombotanyTypes.add(InGameEntityGenerator.getZombieForGame(ZombieType.ZOMBOTANY_PEASHOOTER, 0));
        zombotanyTypes.add(InGameEntityGenerator.getZombieForGame(ZombieType.ZOMBOTANY_WALLNUT, 0));
        zombotanyTypes.add(InGameEntityGenerator.getZombieForGame(ZombieType.ZOMBOTANY_JALAPENO, 0));
        zombotanyTypes.add(InGameEntityGenerator.getZombieForGame(ZombieType.ZOMBOTANY_SQUASH, 0));
        zombotanyTypes.add(InGameEntityGenerator.getZombieForGame(ZombieType.NORMAL, 0));

        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.WAVE_STARTED).build();
        GameEventMessenger.getInstance().dispatch(GameEvent.WAVE_STARTED, payload);
        Random random = new Random();
        while (accumulatedCost < targetDifficulty) {
            Zombie template = zombotanyTypes.get(random.nextInt(zombotanyTypes.size()));
            int lane = random.nextInt(session.getArena().getRows());

            Zombie newZombie = InGameEntityGenerator.getZombieForGame(template.getType(), lane);
            newZombie.setCol(session.getArena().getCols() - 1);

            wave.addZombie(newZombie);
            accumulatedCost += newZombie.getWaveCost();

            session.getArena().addZombie(newZombie);
            session.getTimeManager().registerNewTicker(newZombie);

            notify("Zombotany: " + newZombie.getType().name() + " spawned in lane " + (lane+1));
        }
    }

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.ZOMBOTANY;
    }
}
