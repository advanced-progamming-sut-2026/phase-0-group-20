package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.Random;

public class ZombossMissileAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;
    private final MissileImpactBehavior impactBehavior;

    private int attackTimer;
    private int targetRow;
    private int targetCol;
    private final Random random = new Random();

    private static final int START_DURATION_TICKS = (int) (3.35f * TimeManager.TICKS_PER_SECOND);
    private static final int LAUNCH_DURATION_TICKS = (int) (1.8f * TimeManager.TICKS_PER_SECOND);
    private static final int TOTAL_DURATION_TICKS = START_DURATION_TICKS + LAUNCH_DURATION_TICKS;

    public ZombossMissileAttack(Zomboss zomboss, IdleZombossAttack idleState, MissileImpactBehavior impactBehavior) {
        this.zomboss = zomboss;
        this.idleState = idleState;
        this.impactBehavior = impactBehavior;
    }

    @Override
    public void onEnter() {
        this.attackTimer = 0;

        GameSession session = GameSession.getInstance();
        this.targetRow = random.nextInt(session.getArena().getRows());
        this.targetCol = random.nextInt(session.getArena().getCols() - 2);

        zomboss.setState(ZombieState.BOSS_MISSILE_START);

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("TARGET_LOCKED")
                .coordinate(targetRow, targetCol)
                .build());

        zomboss.notify("Zomboss missile on row " + (targetRow + 1) +"and col " + (targetCol+1) + "!");
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == START_DURATION_TICKS) {
            zomboss.setState(ZombieState.BOSS_MISSILE_LAUNCH);
            launchMissile();
        } else if (attackTimer >= TOTAL_DURATION_TICKS) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }

    private void launchMissile() {
        GameSession session = GameSession.getInstance();

        float endX = targetCol * PhysicalConstants.TILE_WIDTH +
            PhysicalConstants.GRID_START_X + PhysicalConstants.TILE_WIDTH / 2f;
        float endY = (5- targetRow) * PhysicalConstants.TILE_HEIGHT +
            PhysicalConstants.GRID_START_Y + PhysicalConstants.TILE_HEIGHT / 2f;
        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("MISSILE_LAUNCHED")
                .pixelCoordinate(endX, endY)
                .coordinate(targetRow, targetCol)
                .build());
        ZombossMissile missile = new ZombossMissile(endX, endY, targetRow, targetCol, impactBehavior);
        session.getTimeManager().registerNewTicker(missile);
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }
}
