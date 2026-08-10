package io.java.pvz.models.game.minigame;

import io.java.pvz.models.fields.tiles.PlantVaseTile;
import io.java.pvz.models.fields.tiles.RandomVaseTile;
import io.java.pvz.models.fields.tiles.ZombieVaseTile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.adventure.levels.conditions.NormalLoseCondition;
import io.java.pvz.models.game.minigame.minigameCondition.VaseBreakerCondition;

import java.util.Random;

public class VaseBreakerLevel extends Level implements IMinigame {

    private final Random random = new Random();

    private static final int ZOMBIE_CHANCE = 5; // badan mitonim arzyabi konim taghir bedim
    private static final int PLANT_CHANCE = 15;
    private int zombieCount = 0;
    private int plantCount = 0;

    public VaseBreakerLevel(String name, SeasonType seasonType, int waveCount, int levelNumber) {
        super(name, seasonType, waveCount, -1, levelNumber);
        this.addWinCondition(new VaseBreakerCondition());
        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onLevelStart(GameSession session) {

        int rows = session.getArena().getRows();
        int cols = session.getArena().getCols();

        int currentZombieChance = ZOMBIE_CHANCE + (2 * levelNumber);
        int currentPlantChance = Math.max(5, PLANT_CHANCE - levelNumber);

        for (int row = 0; row < rows; row++) {
            for (int col = cols - 5; col < cols; col++) {

                int rnd = random.nextInt(100);

                if (rnd < currentZombieChance && zombieCount < 4) {
                    session.getArena().changeTile(row, col, new ZombieVaseTile(row, col));
                    zombieCount++;
                } else if (rnd < currentPlantChance + currentZombieChance && plantCount < 4) {
                    session.getArena().changeTile(row, col, new PlantVaseTile(row, col));
                    plantCount++;
                } else
                    session.getArena().changeTile(row, col, new RandomVaseTile(row, col));

            }
        }

    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {

    }


    @Override
    public boolean skySunFalls() {
        return false;
    }

    @Override
    public boolean skipsPlantSelection() {
        return true;
    }

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.VASE_BREAKER;
    }
}
