package io.java.pvz.models.fields.modifiers;

import io.java.pvz.models.entities.zombies.Wave;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.fields.tiles.GraveStoneTile;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.Ticker;

import java.util.Random;

public class EgyptModifier implements SeasonModifier {

    Wave currentWave;

    @Override
    public void onCurrentLevelStart() {
        Arena arena = GameSession.getInstance().getArena();
        setupEgyptGraves(arena);
    }

    @Override
    public void onWaveStart(Wave wave) {
        currentWave = wave;
    }

    @Override
    public void onZombieSpawn(Zombie zombie, Arena arena) {
        if (currentWave == null) return;

        if (currentWave.isLastWave()) {
            if (Math.random() < 0.5) {
                int randColAhead = (int) (Math.random() * 4) + 1;
                int startCol = arena.getCols() - 1;
                final int targetCol = Math.max(0, startCol - randColAhead);

                final float targetX = PhysicalConstants.GRID_START_X +
                    (targetCol * PhysicalConstants.TILE_WIDTH) + (PhysicalConstants.TILE_WIDTH / 2f);

                final float startX = PhysicalConstants.GRID_START_X + (13 * PhysicalConstants.TILE_WIDTH);
                // lazem bood taghir bedim 13 ro

                zombie.setX(startX);
                zombie.setSpawnEffect(Zombie.SpawnEffect.SANDSTORM);

                GameEventPayload payload = new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("SANDSTORM_START")
                    .zombie(zombie)
                    .build();
                GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT, payload);

                GameSession.getInstance().getTimeManager().registerNewTicker(new Ticker() {
                    @Override
                    public void onTick(int currentTick) {
                        if (zombie.isDead()) {
                            GameSession.getInstance().getTimeManager().unregisterTicker(this);
                            return;
                        }

                        float currentX = zombie.getX();
                        currentX += (targetX - currentX) * 0.05f;
                        zombie.setX(currentX);

                        if (Math.abs(targetX - currentX) < 3.0f) {
                            zombie.setX(targetX);
                            zombie.setSpawnEffect(Zombie.SpawnEffect.NORMAL);
                            GameSession.getInstance().getTimeManager().unregisterTicker(this);
                        }
                    }
                });
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


        int numberOfGraves = (rand.nextInt(3) + 3) + getCurrentLevelNumber(); //baada age monaseb nabood bishtar mikonim

        int gravesPlanted = 0;
        while (gravesPlanted < numberOfGraves) {
            int randomRow = rand.nextInt(rows);
            int randomCol = rand.nextInt(cols / 2) + (cols / 2);

            if (!(arena.getTile(randomRow, randomCol) instanceof GraveStoneTile)) {
                arena.changeTile(randomRow, randomCol, new GraveStoneTile(randomRow, randomCol));
                gravesPlanted++;
            }
        }
    }
}
