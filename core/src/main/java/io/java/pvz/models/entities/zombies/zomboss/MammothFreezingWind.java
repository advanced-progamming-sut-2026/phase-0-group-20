package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.models.entities.obstacle.IceBlock;
import io.java.pvz.models.entities.obstacle.IceHolder;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;

public class MammothFreezingWind implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;

    private int attackTimer;
    private final int totalDurationTicks = 3 * TimeManager.TICKS_PER_SECOND;

    public MammothFreezingWind(Zomboss zomboss, IdleZombossAttack idleState) {
        this.zomboss = zomboss;
        this.idleState = idleState;
    }

    @Override
    public void onEnter() {
        this.attackTimer = 0;
        zomboss.setState(ZombieState.BOSS_WIND);
        zomboss.notify("Mammoth Zomboss is taking a deep breath of freezing air...");
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == TimeManager.TICKS_PER_SECOND) {
            blowFreezingWind();
        }

        if (attackTimer >= totalDurationTicks) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }

    private void blowFreezingWind() {
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();

        int[] targetRows = {zomboss.getRow(), zomboss.getSecondRow()};

        for (int row : targetRows) {
            if (row < 0 || row >= arena.getRows()) continue;

            zomboss.notify("A massive freezing wind sweeps through lane " + (row + 1) + "!");

            for (Plant plant : new ArrayList<>(arena.getActivePlants())) {
                Tile tile = plant.getPlacedTile();

                if (tile == null || tile.getRow() != row) continue;

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
            session.getArena().getActiveObstacles().add(iceBlock);

            zomboss.notify(plant.getName() + " is completely frozen inside an IceBlock by Zomboss!");
        }
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }
}
