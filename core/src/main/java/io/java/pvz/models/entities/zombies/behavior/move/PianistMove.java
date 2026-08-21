package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.obstacle.Piano;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PianistMove implements MoveBehavior, GameEventListener {
    private final Zombie zombie;
    private final Piano piano;
    private final Random random = new Random();

    private static final float PIANO_OFFSET_X = 45f;

    private static final int PLAY_DURATION_TICKS = 4 * TimeManager.TICKS_PER_SECOND;
    private static final int IDLE_DURATION_TICKS = 8 * TimeManager.TICKS_PER_SECOND;

    private boolean isPlaying = false;
    private int phaseTimer = 0;

    public PianistMove(Zombie zombie, Piano piano) {
        this.zombie = zombie;
        this.piano = piano;
        if (piano != null) {
            piano.setPianistZombie(zombie);
        }

        GameEventMessenger.getInstance().addListener(GameEvent.ZOMBIE_KILLED, this);
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.ZOMBIE_KILLED && payload != null && payload.getZombie() == this.zombie) {
            destroyPianoIfExists();
            GameEventMessenger.getInstance().removeListener(GameEvent.ZOMBIE_KILLED, this);
        }
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        if (zombie.isDead()) {
            destroyPianoIfExists();
            return;
        }

        if (piano != null && !piano.isDestroyed()) {
            phaseTimer++;

            if (isPlaying) {
                zombie.setAttacking(false);
                zombie.setState(ZombieState.SPECIAL);

                float oldX = zombie.getX();
                zombie.move();
                float dx = zombie.getX() - oldX;

                piano.push(dx);
                piano.getPosition().setX(zombie.getX() - PIANO_OFFSET_X);

                crushEntitiesInFrontOfPiano(piano);

                if (phaseTimer >= PLAY_DURATION_TICKS) {
                    isPlaying = false;
                    phaseTimer = 0;
                    triggerZombieRowSwitch();
                }

            } else {
                zombie.setAttacking(false);
                zombie.setState(ZombieState.WALKING);

                piano.getPosition().setX(zombie.getX() - PIANO_OFFSET_X);

                if (phaseTimer >= IDLE_DURATION_TICKS) {
                    isPlaying = true;
                    phaseTimer = 0;
                }
            }
        } else {
            zombie.setState(ZombieState.WALKING);
            zombie.move();
        }
    }

    private void destroyPianoIfExists() {
        if (piano != null && !piano.isDestroyed()) {
            piano.destroyPiano();
        }
    }

    private void triggerZombieRowSwitch() {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        List<ZombieType> noEffects = Arrays.asList(
            ZombieType.ZOMBOSS_BEACH,
            ZombieType.ZOMBOSS_EGYPT,
            ZombieType.ZOMBOSS_DARK_AGES,
            ZombieType.ZOMBOSS_FROZEN_CAVES,
            ZombieType.GARGANTUAR
        );

        List<Zombie> zombies = session.getArena().getActiveZombies();
        for (Zombie z : zombies) {
            if (z != null && !z.isDead() && z.getType() != ZombieType.PIANIST && !noEffects.contains(z.getType())) {
                int currentRow = z.getRow();
                List<Integer> possibleRows = new ArrayList<>();
                if (currentRow > 0) possibleRows.add(currentRow - 1);
                if (currentRow < 4) possibleRows.add(currentRow + 1);

                int newRow = possibleRows.get(random.nextInt(possibleRows.size()));
                z.setRow(newRow);
            }
        }
    }

    private void crushEntitiesInFrontOfPiano(Piano piano) {
        GameSession session = GameSession.getInstance();
        if (session == null || session.getArena() == null) return;

        List<Plant> plantsToCrush = new ArrayList<>();
        Tile tile = session.getArena().getTile(piano.getRow(), piano.getCol());
        if (tile != null && !tile.getPlants().isEmpty()) {
            plantsToCrush.addAll(tile.getPlants());
        }

        for (Plant plant : session.getArena().getActivePlants()) {
            if (plant != null && !plant.isDead() && plant.getPlacedTile() != null) {
                if (plant.getPlacedTile().getRow() == piano.getRow()) {
                    float dist = Math.abs(plant.getPosition().getX() - piano.getX());
                    if (dist <= PhysicalConstants.TILE_WIDTH * 0.75f) {
                        if (!plantsToCrush.contains(plant)) {
                            plantsToCrush.add(plant);
                        }
                    }
                }
            }
        }

        for (Plant plant : plantsToCrush) {
            if (plant != null && !plant.isDead()) {
                plant.takeDamage(99999);
                if (plant.getPlacedTile() != null) {
                    plant.getPlacedTile().getPlants().remove(plant);
                }
                session.getArena().getActivePlants().remove(plant);
                session.getTimeManager().unregisterTicker(plant);
            }
        }

        List<Zombie> zombiesToCrush = new ArrayList<>();
        for (Zombie z : session.getArena().getActiveZombies()) {
            if (z != null && z.isHypnotized() && !z.isDead() && z.getRow() == piano.getRow()) {
                float dist = Math.abs(z.getX() - piano.getX());
                if (dist <= PhysicalConstants.TILE_WIDTH * 0.75f) {
                    zombiesToCrush.add(z);
                }
            }
        }

        for (Zombie z : zombiesToCrush) {
            if (z != null && !z.isDead()) {
                z.takeDamage(99999);
            }
        }
    }

    public Piano getPiano() {
        return piano;
    }
}
