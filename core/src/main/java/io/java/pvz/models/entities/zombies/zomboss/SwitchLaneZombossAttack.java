package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.Random;

public class SwitchLaneZombossAttack implements IZombossAttack {
    private static final float ANIMATION_DURATION = 1.23f;

    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;
    private int moveTimer;
    private int targetRow;
    private float startY;
    private float targetY;
    private final Random random = new Random();

    public SwitchLaneZombossAttack(Zomboss zomboss, IdleZombossAttack idleState) {
        this.zomboss = zomboss;
        this.idleState = idleState;
    }

    @Override
    public void onEnter() {
        this.moveTimer = 0;

        int maxRows = GameSession.getInstance().getArena().getRows();
        int currentRow = zomboss.getRow();

        do {
            targetRow = random.nextInt(maxRows - 1);
        } while (targetRow == currentRow);

        if (targetRow > currentRow) {
            zomboss.setState(ZombieState.ZOMBOSS_WALK_UP);
        } else {
            zomboss.setState(ZombieState.ZOMBOSS_WALK_DOWN);
        }

        this.startY = zomboss.getY();
        this.targetY = targetRow * PhysicalConstants.TILE_HEIGHT + PhysicalConstants.GRID_START_Y;
    }

    @Override
    public void execute() {
        moveTimer++;
        int animationTicks = (int) (ANIMATION_DURATION * TimeManager.TICKS_PER_SECOND);

        float progress = (float) moveTimer / animationTicks;
        if (progress > 1.0f) {
            progress = 1.0f;
        }

        float currentY = startY + (targetY - startY) * progress;
        zomboss.setY(currentY);

        if (moveTimer >= animationTicks) {
            zomboss.setRow(targetRow);
            zomboss.setSecondRow(targetRow + 1);
            zomboss.setY(targetY);

            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
            zomboss.setState(ZombieState.BOSS_IDLE);
        }
    }

    @Override
    public void onExit() {
        this.moveTimer = 0;
    }
}
