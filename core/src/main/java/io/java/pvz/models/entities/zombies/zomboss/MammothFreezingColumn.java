package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.models.entities.obstacle.IceBlock;
import io.java.pvz.models.entities.obstacle.IceHolder;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MammothFreezingColumn implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;
    private final List<ZombieType> allowedZombies;

    private int attackTimer;
    private int targetCol;

    private static final int FREEZE_EXECUTION_TICKS = 2 * TimeManager.TICKS_PER_SECOND;
    private static final int TOTAL_DURATION_TICKS = (int) (6.5 * TimeManager.TICKS_PER_SECOND);

    private final Random random = new Random();

    public MammothFreezingColumn(Zomboss zomboss, IdleZombossAttack idleState, List<ZombieType> allowedZombies) {
        this.zomboss = zomboss;
        this.idleState = idleState;
        this.allowedZombies = allowedZombies;
    }

    public int getTargetCol() {
        return targetCol;
    }

    @Override
    public void onEnter() {
        this.attackTimer = 0;
        this.targetCol = random.nextInt(6);
        zomboss.setState(ZombieState.BOSS_GLACIER);

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("MAMMOTH_TRUNK_TARGET")
                .coordinate(0, targetCol)
                .build());

        zomboss.notify("Mammoth Zomboss started continuous freeze animation on column " + (targetCol + 1) + "!");
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == FREEZE_EXECUTION_TICKS) {
            zomboss.notify("Mammoth Zomboss logic is freezing column " + (targetCol + 1) + " now!");
            spawnFrozenZombiesInColumn();
            freezeTargetColumn();
        } else if (attackTimer >= TOTAL_DURATION_TICKS) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }

    private void spawnFrozenZombiesInColumn() {
        if (allowedZombies == null || allowedZombies.isEmpty()) return;

        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();
        List<Integer> emptyRows = new ArrayList<>();
        List<Integer> occupiedRows = new ArrayList<>();

        for (int r = 0; r < arena.getRows(); r++) {
            Tile t = arena.getTile(r, targetCol);
            if (t instanceof IceHolder iceHolder && !iceHolder.hasIceBlock()) {
                if (t.getPlants().isEmpty()) {
                    emptyRows.add(r);
                } else {
                    occupiedRows.add(r);
                }
            }
        }
        Collections.shuffle(emptyRows, random);
        Collections.shuffle(occupiedRows, random);

        List<Integer> selectedRows = new ArrayList<>();

        for (int r : emptyRows) {
            if (selectedRows.size() < 1) selectedRows.add(r);
        }

        for (int r : occupiedRows) {
            if (selectedRows.size() < 1) selectedRows.add(r);
        }

        int spawnedCount = 0;
        makeIceBlocks(selectedRows, arena, session, spawnedCount);
    }

    private void makeIceBlocks(List<Integer> selectedRows, Arena arena, GameSession session, int spawnedCount) {
        for (int row : selectedRows) {
            Tile tile = arena.getTile(row, targetCol);

            if (!tile.getPlants().isEmpty()) {
                for (Plant plant : new ArrayList<>(tile.getPlants())) {
                    session.getTimeManager().unregisterTicker(plant);
                    arena.getActivePlants().remove(plant);
                }
                tile.getPlants().clear();
            }

            if (tile instanceof IceHolder iceHolder) {
                ZombieType type = allowedZombies.get(random.nextInt(allowedZombies.size()));
                Zombie newZombie = InGameEntityGenerator.getZombieForGame(type, row);

                if (newZombie != null) {
                    newZombie.setCol(targetCol);
                    IceBlock iceBlock = new IceBlock(newZombie, row, targetCol);
                    iceHolder.setIceBlock(iceBlock);

                    session.getTimeManager().registerNewTicker(iceBlock);
                    arena.getActiveObstacles().add(iceBlock);

                    spawnedCount++;
                }
            }
        }

        if (spawnedCount > 0) {
            zomboss.notify("Mammoth Zomboss trapped " + spawnedCount +
                " zombies inside IceBlocks in column " + (targetCol + 1) + "!");
        }
    }

    private void freezeTargetColumn() {
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();

        for (int row = 0; row < arena.getRows(); row++) {
            Tile tile = arena.getTile(row, targetCol);
            if (tile == null) continue;

            for (Plant plant : new ArrayList<>(tile.getPlants())) {
                if (plant.getTags().contains(PlantTag.FIRE)) continue;
                instantFreezePlant(plant, arena, session, tile);
            }
        }
    }

    private void instantFreezePlant(Plant plant, Arena arena, GameSession session, Tile tile) {
        int row = tile.getRow();
        int col = tile.getCol();

        session.getTimeManager().unregisterTicker(plant);
        arena.getActivePlants().remove(plant);
        tile.getPlants().remove(plant);

        if (tile instanceof IceHolder iceHolder) {
            IceBlock iceBlock = new IceBlock(plant, row, col);
            iceHolder.setIceBlock(iceBlock);

            session.getTimeManager().registerNewTicker(iceBlock);
            arena.getActiveObstacles().add(iceBlock);
        }
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }

    public void reset() {
        this.attackTimer = 0;
    }
}
