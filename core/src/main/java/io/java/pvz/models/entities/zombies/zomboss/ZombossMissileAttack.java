package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.Random;

public class ZombossMissileAttack implements IZombossAttack{
    private final Zomboss zomboss;
    private final IdleZombossAttack idleState;
    private final MissileImpactBehavior impactBehavior;

    private int attackTimer;
    private int targetRow;
    private int targetCol;
    private final Random random = new Random();

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
        this.targetCol = random.nextInt(session.getArena().getCols()-2);

        zomboss.setState(ZombieState.SPECIAL);

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("TARGET_LOCKED")
                .coordinate(targetRow, targetCol)
                .build());

        zomboss.notify("Zomboss locked a missile on row " + (targetRow + 1) + "!");
    }

    @Override
    public void execute() {
        attackTimer++;

        if (attackTimer == 2 * TimeManager.TICKS_PER_SECOND) {
            launchMissile();
        }

        if (attackTimer >= 3 * TimeManager.TICKS_PER_SECOND) {
            this.onExit();
            idleState.onEnter();
            zomboss.setAttackBehavior(idleState);
        }
    }

    private void launchMissile() {
        GameSession session = GameSession.getInstance();

        float endX = targetCol * PhysicalConstants.TILE_WIDTH +
            PhysicalConstants.GRID_START_X + PhysicalConstants.TILE_WIDTH/2 ;
        float endY = targetRow * PhysicalConstants.TILE_HEIGHT +
            PhysicalConstants.GRID_START_Y +  PhysicalConstants.TILE_HEIGHT/2;

        ZombossMissile missile = new ZombossMissile(endX, endY, targetRow, targetCol, impactBehavior);
        session.getTimeManager().registerNewTicker(missile);
    }

    @Override
    public void onExit() {
        this.attackTimer = 0;
    }


}
