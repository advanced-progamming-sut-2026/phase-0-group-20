package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.Random;

public class DragonFireballAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;
    private int attackTimer;
    private final Random random = new Random();

    public DragonFireballAttack(Zomboss zomboss, IdleZombossAttack idleState) {
        this.zomboss = zomboss;
        this.idleState = idleState;
    }

    @Override
    public void onEnter() {
        this.attackTimer = 0;
        // change state
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == TimeManager.TICKS_PER_SECOND) {
            launchFireballs();
        }

        if (attackTimer >= 3 * TimeManager.TICKS_PER_SECOND) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }

    private void launchFireballs() {
        GameSession session = GameSession.getInstance();
        int cols = session.getArena().getCols();
        int rows = session.getArena().getRows();

        int targetCount = 2 + random.nextInt(2);

        for (int i = 0; i < targetCount; i++) {
            int targetCol = random.nextInt(cols);
            int targetRow = random.nextInt(rows);

            float startX = zomboss.getX() - (PhysicalConstants.TILE_UNIT_LENGTH / 2f);
            float startY = zomboss.getY() + 40f;

            float endX = targetCol * PhysicalConstants.TILE_UNIT_LENGTH;
            float endY = targetRow * PhysicalConstants.TILE_HEIGHT;

            ZombossFireball fireball = new ZombossFireball(startX, startY, endX, endY, targetCol, targetRow);
            session.getTimeManager().registerNewTicker(fireball);

            //add it to the arena
            // session.getArena().addProjectile(fireball);
        }
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }


}
