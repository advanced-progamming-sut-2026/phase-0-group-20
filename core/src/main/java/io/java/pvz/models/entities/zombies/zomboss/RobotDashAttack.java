package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.timeManager.TimeManager;

public class RobotDashAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;

    private boolean isDashing;
    private int jumpTimer;

    private static final float DASH_SPEED_SCALE_PER_TICK = 12.0f;

    public RobotDashAttack(Zomboss zomboss, IdleZombossAttack idleState) {
        this.zomboss = zomboss;
        this.idleState = idleState;
    }

    @Override
    public void onEnter() {
        this.isDashing = true;
        this.jumpTimer = 0;

        zomboss.setState(ZombieState.BOSS_DASH);
        zomboss.notify("Robot Zomboss is dashing forward!");
    }

    @Override
    public void execute() {
        if (isDashing) {
            float newX = zomboss.getX() - DASH_SPEED_SCALE_PER_TICK;
            zomboss.setX(newX);

            int newCol = (int) ((newX - PhysicalConstants.GRID_START_X) / PhysicalConstants.TILE_WIDTH);

            if (newCol != zomboss.getCol()) {
                zomboss.setCol(newCol);
            }

            if (newX <= 0) {
                isDashing = false;
                zomboss.notify("Robot Zomboss reached the end and is jumping back!");
                zomboss.setState(ZombieState.BOSS_JUMP);
            }

        } else {
            jumpTimer++;
            if (jumpTimer >= TimeManager.TICKS_PER_SECOND) {
                zomboss.setCol(8);

                zomboss.notify("Robot Zomboss landed on column 9!");

                this.onExit();
                idleState.onEnter();
                zomboss.setAttackBehavior(idleState);
            }
        }
    }

    @Override
    public void onExit() {
        this.isDashing = false;
        this.jumpTimer = 0;
    }
}
