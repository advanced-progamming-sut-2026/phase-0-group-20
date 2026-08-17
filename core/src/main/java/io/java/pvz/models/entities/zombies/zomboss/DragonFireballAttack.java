package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.Random;

public class DragonFireballAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;
    private int attackTimer;
    private final Random random = new Random();

    private static final int START_TICKS = (int) (1.85f * TimeManager.TICKS_PER_SECOND);
    private static final int LOOP_TICKS = (int) (0.53f * 2 * TimeManager.TICKS_PER_SECOND);
    private static final int END_TICKS = (int) (0.95f * TimeManager.TICKS_PER_SECOND);

    private static final int PHASE_1_END = START_TICKS;
    private static final int PHASE_2_END = START_TICKS + LOOP_TICKS;
    private static final int TOTAL_TICKS = START_TICKS + LOOP_TICKS + END_TICKS;

    public DragonFireballAttack(Zomboss zomboss, IdleZombossAttack idleState) {
        this.zomboss = zomboss;
        this.idleState = idleState;
    }

    @Override
    public void onEnter() {
        this.attackTimer = 0;
        zomboss.setState(ZombieState.BOSS_FIREBOMB_START);
        zomboss.notify("Dragon Zomboss is launching fireballs!");
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == PHASE_1_END) {
            zomboss.setState(ZombieState.BOSS_FIREBOMB_LOOP);
            launchFireballs();
        } else if (attackTimer == PHASE_2_END) {
            zomboss.setState(ZombieState.BOSS_FIREBOMB_END);
        } else if (attackTimer >= TOTAL_TICKS) {
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

            float startX = zomboss.getX() - (PhysicalConstants.TILE_WIDTH / 2f);
            float startY = zomboss.getY() + 40f;

            float endX = targetCol * PhysicalConstants.TILE_WIDTH +
                PhysicalConstants.GRID_START_X + PhysicalConstants.TILE_WIDTH / 2f;
            float endY = (5 - targetRow) * PhysicalConstants.TILE_HEIGHT +
                PhysicalConstants.GRID_START_Y + PhysicalConstants.TILE_HEIGHT / 2f;

            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("FIREBALL_LAUNCHED")
                    .pixelCoordinate(endX, endY)
                    .coordinate(targetRow, targetCol)
                    .build());

            ZombossFireball fireball = new ZombossFireball(startX, startY, endX, endY, targetCol, targetRow);
            session.getTimeManager().registerNewTicker(fireball);
        }
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }
}
