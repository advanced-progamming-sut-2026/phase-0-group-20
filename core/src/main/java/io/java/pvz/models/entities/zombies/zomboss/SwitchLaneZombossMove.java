package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.Random;

public class SwitchLaneZombossMove implements IZombossMove {
    private final Zomboss zomboss;
    private IdleZombossMove idleMove;
    private int moveTimer;
    private final Random random = new Random();

    public SwitchLaneZombossMove(Zomboss zomboss) {
        this.zomboss = zomboss;
    }

    public void setIdleMove(IdleZombossMove idleMove) {
        this.idleMove = idleMove;
    }

    @Override
    public void onEnter() {
        this.moveTimer = 0;
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
            zomboss.notify("Zomboss switched to rows " + (newRow + 1) + " and " + (newRow + 2) + "!");
        }

        if (moveTimer >= 2 * TimeManager.TICKS_PER_SECOND) {
            this.onExit();
            idleMove.onEnter();
            zomboss.setMoveBehavior(idleMove);
        }
    }

    @Override
    public void onExit() {
    }

    @Override
    public void reset() {
        this.moveTimer = 0;
    }
}
