package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public class DragonScorchedEarthAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;
    private int attackTimer;
    private final int totalDurationTicks = 3 * TimeManager.TICKS_PER_SECOND;

    public DragonScorchedEarthAttack(Zomboss zomboss, IdleZombossAttack idleState) {
        this.zomboss = zomboss;
        this.idleState = idleState;
    }

    @Override
    public void onEnter() {
        this.attackTimer = 0;
        zomboss.setState(ZombieState.BOSS_FIRE_ROW);
        zomboss.notify("Dragon Zomboss takes a deep breath...");
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == TimeManager.TICKS_PER_SECOND) {
            burnRows();
        }

        if (attackTimer >= totalDurationTicks) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }

    private void burnRows() {
        GameSession session = GameSession.getInstance();
        int cols = session.getArena().getCols();

        int[] targetRows = {zomboss.getRow(), zomboss.getSecondRow()};

        for (int r : targetRows) {
            if (r < 0 || r >= session.getArena().getRows()) continue;

            for (int c = 0; c < cols; c++) {
                Tile tile = session.getArena().getTile(r, c);
                burnTheTile(tile);
            }

            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("SCORCHED_EARTH_ROW")
                    .coordinate(r, 0)
                    .build());
        }

        zomboss.notify("Dragon Zomboss scorched rows " + (targetRows[0] + 1) + " and " + (targetRows[1] + 1) + "!");
    }

    static void burnTheTile(Tile tile) {
        if (tile != null) {
            List<Plant> plantsToDestroy = new ArrayList<>(tile.getPlants());
            for (Plant p : plantsToDestroy) {
                p.takeDamage(99999);
            }

            tile.setCrater(true);
            tile.setCraterTimer(4 * TimeManager.TICKS_PER_SECOND);
        }
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }
}
