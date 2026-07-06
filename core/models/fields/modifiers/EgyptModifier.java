package models.fields.modifiers;

import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.fields.tiles.GraveStoneTile;
import models.game.Arena;
import models.game.GameSession;

import java.util.Random;

public class EgyptModifier implements SeasonModifier {

    private boolean isInitialized = false;
    Wave currentWave;

    @Override
    public void onWaveStart(Wave wave) {
        currentWave = wave;
        if (!isInitialized) {
            setupEgyptGraves(GameSession.getInstance().getArena());
            isInitialized = true;
        }
    }

    @Override
    public void onZombieSpawn(Zombie zombie, Arena arena) {
        if (currentWave == null) return;

        if (currentWave.isLastWave()) {
            if (Math.random() < 0.5) {
                int randColAhead = (int) (Math.random() * 4) + 1;
                int startCol = arena.getCols() - 1;
                int targetCol = Math.max(0, startCol - randColAhead);

                zombie.setX(targetCol);

                zombie.setSpawnEffect(Zombie.SpawnEffect.SANDSTORM); //for graphic phase

                System.out.println("A zombie was spawned by a tornado to column " + targetCol + "!");
            }

        }
    }

    @Override
    public void updateEnvironment(int currentTick, Arena arena) {
        // nothing will change during the game flow, just during zombie wave
    }

    private void setupEgyptGraves(Arena arena) {
        Random rand = new Random();
        int rows = arena.getRows();
        int cols = arena.getCols();

        int numberOfGraves = rand.nextInt(3) + 3;

        int gravesPlanted = 0;
        while (gravesPlanted < numberOfGraves) {
            int randomRow = rand.nextInt(rows);
            int randomCol = rand.nextInt(cols / 2) + (cols / 2);

            if (!(arena.getTile(randomRow, randomCol) instanceof GraveStoneTile)) {
                arena.changeTile(randomRow, randomCol, new GraveStoneTile(randomRow, randomCol));
                gravesPlanted++;
            }
        }
        System.out.println(numberOfGraves + " graves generated for Egypt!");
    }
}
