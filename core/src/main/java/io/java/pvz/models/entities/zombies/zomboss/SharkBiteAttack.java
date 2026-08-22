package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SharkBiteAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;

    private int attackTimer;

    private final Random random = new Random();

    private static final int SHOOT_TICK = (int) (0.7 * TimeManager.TICKS_PER_SECOND);
    private static final int TOTAL_DURATION = 3 * TimeManager.TICKS_PER_SECOND;

    public SharkBiteAttack(Zomboss zomboss, IdleZombossAttack idleState) {
        this.zomboss = zomboss;
        this.idleState = idleState;
    }

    @Override
    public void onEnter() {
        this.attackTimer = 0;
        zomboss.setState(ZombieState.BOSS_SHARK);
        zomboss.notify("Zomboss is preparing to release a swarm of mechanical sharks!");
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == SHOOT_TICK) {
            shootSharks();
        } else if (attackTimer >= TOTAL_DURATION) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }

    private void shootSharks() {
        GameSession session = GameSession.getInstance();
        int maxRows = session.getArena().getRows();

        int sharkCount = 2 + random.nextInt(3);

        List<Integer> availableRows = new ArrayList<>();
        for (int i = 0; i < maxRows; i++) {
            availableRows.add(i);
        }
        Collections.shuffle(availableRows);

        float startX = zomboss.getX() - 30f;
        float sharkSpeed = -4.0f;

        for (int i = 0; i < sharkCount && i < availableRows.size(); i++) {
            int row = availableRows.get(i);

            float startY = row * PhysicalConstants.TILE_HEIGHT;

            Position spawnPos = new Position(row, zomboss.getCol());
            spawnPos.setPosition(startX, startY);

            Projectile.spawnZombieProjectile(
                zomboss,
                ProjectileType.SHARK,
                99999,
                spawnPos,
                sharkSpeed,
                0f,
                false,
                false
            );
        }

        zomboss.notify(sharkCount + " Sharks launched in multiple rows!");
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }

    public void reset() {
        this.attackTimer = 0;
    }
}
