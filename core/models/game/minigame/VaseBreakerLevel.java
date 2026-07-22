package models.game.minigame;

import models.fields.tiles.PlantVaseTile;
import models.fields.tiles.RandomVaseTile;
import models.fields.tiles.ZombieVaseTile;
import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;
import models.game.adventure.levels.conditions.NormalLoseCondition;
import models.game.minigame.minigameCondition.VaseBreakerCondition;

import java.util.Random;

public class VaseBreakerLevel extends Level implements IMinigame{

    private final Random random = new Random();

    private final int ZOMBIE_CHANCE = 30; // badan mitonim arzyabi konim taghir bedim
    private final int PLANT_CHANCE = 30;

    public VaseBreakerLevel( String name, SeasonType seasonType , int waveCount,int levelNumber ) {
        super(name, seasonType, waveCount, -1, levelNumber);
        this.addWinCondition(new VaseBreakerCondition());
        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onStart(GameSession session) {
        notify("VaseBreaker Level " + levelNumber + " Started!");

        int rows = session.getArena().getRows();
        int cols = session.getArena().getCols();

        int currentZombieChance = ZOMBIE_CHANCE + (5 * levelNumber);
        int currentPlantChance = Math.max(0, PLANT_CHANCE - (5 * levelNumber));

        for (int row = 0; row < rows; row++) {
            for (int col = cols - 5; col < cols; col++) {

                int rnd = random.nextInt(100);

                if (rnd < currentZombieChance)
                    session.getArena().changeTile(row, col, new ZombieVaseTile(row, col));
                else if (rnd < currentPlantChance + currentZombieChance)
                    session.getArena().changeTile(row, col, new PlantVaseTile(row, col));
                else
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
