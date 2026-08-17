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

    private static final int JUMP_START_TICKS = (int) (0.7f * TimeManager.TICKS_PER_SECOND);
    private static final int JUMP_MID_TICKS = (int) (0.45f * TimeManager.TICKS_PER_SECOND);
    private static final int JUMP_LAND_TICKS = (int) (0.7f * TimeManager.TICKS_PER_SECOND);

    private float jumpStartX;
    private float targetX;

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

            if (newX <= PhysicalConstants.GRID_START_X + 50f) {
                isDashing = false;
                jumpTimer = 0;

                jumpStartX = zomboss.getX();
                targetX = 8 * PhysicalConstants.TILE_WIDTH + PhysicalConstants.GRID_START_X;

                zomboss.setState(ZombieState.BOSS_JUMP_START);
                zomboss.notify("Robot Zomboss reached the end and is preparing to jump!");
            }

        } else {
            jumpTimer++;

            if (jumpTimer <= JUMP_START_TICKS) {
                if (zomboss.getState() != ZombieState.BOSS_JUMP_START) {
                    zomboss.setState(ZombieState.BOSS_JUMP_START);
                }
            }
            else if (jumpTimer <= JUMP_START_TICKS + JUMP_MID_TICKS) {
                if (zomboss.getState() != ZombieState.BOSS_JUMP_MID) {
                    zomboss.setState(ZombieState.BOSS_JUMP_MID);
                }

                int midTicks = jumpTimer - JUMP_START_TICKS;
                float progress = (float) midTicks / JUMP_MID_TICKS;

                float newX = jumpStartX + (targetX - jumpStartX) * progress;
                zomboss.setX(newX);

                int newCol = (int) ((newX - PhysicalConstants.GRID_START_X) / PhysicalConstants.TILE_WIDTH);
                if (newCol != zomboss.getCol()) {
                    zomboss.setCol(newCol);
                }

            }
            else if (jumpTimer <= JUMP_START_TICKS + JUMP_MID_TICKS + JUMP_LAND_TICKS) {
                if (zomboss.getState() != ZombieState.BOSS_JUMP_LAND) {
                    zomboss.setState(ZombieState.BOSS_JUMP_LAND);
                    zomboss.setX(targetX);
                    zomboss.setCol(8);
                    zomboss.notify("Robot Zomboss landed on column 9!");
                }
            }
            else {
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
