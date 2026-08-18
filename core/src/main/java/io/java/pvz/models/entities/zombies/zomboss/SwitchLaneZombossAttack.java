package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.Random;

public class SwitchLaneZombossAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;
    private int moveTimer;
    private final Random random = new Random();

    public SwitchLaneZombossAttack(Zomboss zomboss, IdleZombossAttack idleState) {
        this.zomboss = zomboss;
        this.idleState = idleState;
    }

    @Override
    public void onEnter() {
        this.moveTimer = 0;
        zomboss.setState(ZombieState.WALKING);
        zomboss.notify("Zomboss is preparing to switch lanes...");
    }

    @Override
    public void execute() {
        moveTimer++;

        if (moveTimer == TimeManager.TICKS_PER_SECOND) {
            int maxRows = GameSession.getInstance().getArena().getRows();
            int currentRow = zomboss.getRow();
            int newRow;

            do {
                newRow = random.nextInt(maxRows - 1);
            } while (newRow == currentRow);

            zomboss.setRow(newRow);
            zomboss.setSecondRow(newRow + 1);

            float newY = newRow * PhysicalConstants.TILE_HEIGHT +
                PhysicalConstants.GRID_START_Y;
            zomboss.setY(newY);

            zomboss.notify("Zomboss switched to rows " + (newRow + 1) + " and " + (newRow + 2) + "!");
        }

        if (moveTimer >= 2 * TimeManager.TICKS_PER_SECOND) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }

    @Override
    public void onExit() {
        this.moveTimer = 0;
    }
}
