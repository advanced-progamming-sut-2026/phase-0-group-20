package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

public class ZombossSpawnZombiesAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;
    private final List<ZombieType> allowedZombies;

    private int attackTimer;
    private final int spawnTicks;
    private final int endTicks;
    private final int totalTicks;

    private final Random random = new Random();

    public ZombossSpawnZombiesAttack(Zomboss zomboss, IdleZombossAttack idleState, List<ZombieType> allowedZombies) {
        this.zomboss = zomboss;
        this.idleState = idleState;
        this.allowedZombies = allowedZombies;

        switch (zomboss.getType()) {
            case ZOMBOSS_EGYPT -> {
                this.spawnTicks = (int) (2.2f * TimeManager.TICKS_PER_SECOND);
                this.endTicks = (int) ((2.2f + 2.0f) * TimeManager.TICKS_PER_SECOND);
                this.totalTicks = (int) ((2.3f + 2.0f + 1.5f) * TimeManager.TICKS_PER_SECOND);
            }
            case ZOMBOSS_FROZEN_CAVES -> {
                this.spawnTicks = (int) (1.5f * TimeManager.TICKS_PER_SECOND);
                this.endTicks = (int) (2.5f * TimeManager.TICKS_PER_SECOND);
                this.totalTicks = (int) (3.5f * TimeManager.TICKS_PER_SECOND);
            }
            case ZOMBOSS_DARK_AGES -> {
                this.spawnTicks = (int) (1.0f * TimeManager.TICKS_PER_SECOND);
                this.endTicks = (int) (1.6f * TimeManager.TICKS_PER_SECOND);
                this.totalTicks = (int) (2.15f * TimeManager.TICKS_PER_SECOND);
            }
            case ZOMBOSS_BEACH -> {
                this.spawnTicks = (int) (1.5f * TimeManager.TICKS_PER_SECOND);
                this.endTicks = (int) (2.2f * TimeManager.TICKS_PER_SECOND);
                this.totalTicks = (int) (3.0f * TimeManager.TICKS_PER_SECOND);
            }
            default -> {
                this.spawnTicks = 1 * TimeManager.TICKS_PER_SECOND;
                this.endTicks = 2 * TimeManager.TICKS_PER_SECOND;
                this.totalTicks = 3 * TimeManager.TICKS_PER_SECOND;
            }
        }
    }

    @Override
    public void onEnter() {
        this.attackTimer = 0;
        zomboss.setState(ZombieState.BOSS_SUMMON_START);
        zomboss.notify("Zomboss is summoning guards!");
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == spawnTicks) {
            zomboss.setState(ZombieState.BOSS_SUMMON_LOOP);
            spawnZombies();
        } else if (attackTimer == endTicks) {
            zomboss.setState(ZombieState.BOSS_SUMMON_END);
        } else if (attackTimer >= totalTicks) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }

    private void spawnZombies() {
        if (allowedZombies == null || allowedZombies.isEmpty()) {
            return;
        }

        GameSession session = GameSession.getInstance();
        int maxRows = session.getArena().getRows();

        int centerRow = zomboss.getRow();
        int[] targetRows = {centerRow - 1, centerRow, centerRow + 1};

        int spawnCol = zomboss.getCol();
        float zombossBaseX = zomboss.getX();
        int spawnedCount = 0;

        for (int targetRow : targetRows) {
            if (targetRow >= 0 && targetRow < maxRows) {
                ZombieType randomType = allowedZombies.get(random.nextInt(allowedZombies.size()));
                Zombie newZombie = InGameEntityGenerator.getZombieForGame(randomType, targetRow);

                newZombie.setCol(spawnCol);

                float randomXOffset = (random.nextFloat() * 60f) - 30f;
                newZombie.setX(zombossBaseX + randomXOffset);

                session.getArena().addZombie(newZombie);
                session.getTimeManager().registerNewTicker(newZombie);

                spawnedCount++;
            }
        }

        zomboss.notify("Zomboss summoned " + spawnedCount + " zombies to protect itself!");
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }
}
