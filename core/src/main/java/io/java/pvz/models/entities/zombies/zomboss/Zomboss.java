package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieData;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.entities.zombies.behavior.attack.AttackBehavior;
import io.java.pvz.models.entities.zombies.behavior.defense.DefenseBehavior;
import io.java.pvz.models.entities.zombies.behavior.effect.ZombieEffect;
import io.java.pvz.models.entities.zombies.behavior.move.MoveBehavior;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public abstract class Zomboss extends Zombie {
    protected int phase = 3;
    protected int phaseHealth;
    protected int currentPhaseHealth;
    protected int secondRow;
    protected Tile secondTile;
    protected int stunDurationTicks = 4 * TimeManager.TICKS_PER_SECOND;
    protected int currentStunTicks = 0;

    public Zomboss(ZombieType type, int row,
                   MoveBehavior moveBehavior,
                   AttackBehavior attackBehavior,
                   DefenseBehavior defenseBehavior) {
        ZombieData data = new ZombieData(
            "Zomboss"
            , 9000
            , 0.3f
            , 1000
            , 1000
            , false
            , 1000
            , ""
            , null
        );
        super(type, data, row, moveBehavior, attackBehavior, defenseBehavior);
        this.secondRow = row + 1;
        this.phaseHealth = this.getBaseHp() / 3;
        this.currentPhaseHealth = this.phaseHealth;
        this.setCol(8);
        init();
    }

    public abstract void init();

    @Override
    public void onTick(int currentTick) {
        if (isDead()) return;

        List<ZombieEffect> snapshot = new ArrayList<>(getActiveEffects());
        for (ZombieEffect effect : snapshot) {
            effect.execute();
        }

        if (getState() == ZombieState.STUNNED) {
            currentStunTicks--;
            if (currentStunTicks <= 0) {
                if (phase == 2) {
                    GameEventMessenger.getInstance().dispatch(GameEvent.ZOMBOSS_PHASE_2,
                        new GameEventPayload.Builder(GameEvent.ZOMBOSS_PHASE_2).build());
                } else if (phase == 1) {
                    GameEventMessenger.getInstance().dispatch(GameEvent.ZOMBOSS_PHASE_3,
                        new GameEventPayload.Builder(GameEvent.ZOMBOSS_PHASE_3).build());
                }
                this.setState(ZombieState.WALKING);
            }
            return;
        }

        if (getMoveBehavior() != null) {
            getMoveBehavior().execute();
        }

        if (getAttackBehavior() != null) {
            getAttackBehavior().execute();
        }

        updateTile();
    }

    @Override
    protected void updateTile() {
        super.updateTile();
        int column = this.getCol();
        if (GameSession.getInstance() != null && GameSession.getInstance().getArena() != null) {
            if (this.secondRow < GameSession.getInstance().getArena().getRows()) {
                this.secondTile = GameSession.getInstance().getArena().getTile(this.secondRow, column);
            } else {
                this.secondTile = null;
            }
        }
    }

    @Override
    public void takeDamage(int damage, Projectile projectile) {
        damage = Math.min(500, damage);

        if (getDefenseBehavior() != null) {
            int mitigatedDamage = getDefenseBehavior()
                .mitigateDamage(damage, projectile != null ? projectile.getType() : null);
            super.takeDamage(mitigatedDamage, projectile);
        } else {
            super.takeDamage(damage, projectile);
        }
    }

    @Override
    public void takeDamage(int damage) {
        damage = Math.min(500, damage);
        if (getDefenseBehavior() != null) {
            int mitigatedDamage = getDefenseBehavior().mitigateDamage(damage, null);
            super.takeDamage(mitigatedDamage);
        } else {
            super.takeDamage(damage);
        }
    }

    public void triggerStun() {
        this.setState(ZombieState.STUNNED);
        this.currentStunTicks = stunDurationTicks;
        this.phase--;
        this.currentPhaseHealth = this.phaseHealth;
        GameSession.notify(this.getName() + " is STUNNED! Phase " + phase + " started.");
    }

    @Override
    public boolean isOccupyingRow(int targetRow) {
        return this.getRow() == targetRow || this.getSecondRow() == targetRow;
    }

    public void reducePhaseHealth(int amount) {
        this.currentPhaseHealth -= amount;
    }

    public int getCurrentPhaseHealth() {
        return currentPhaseHealth;
    }

    public int getPhase() {
        return phase;
    }

    public int getSecondRow() {
        return secondRow;
    }

    public void setSecondRow(int secondRow) {
        this.secondRow = secondRow;
        updateTile();
    }

    public Tile getSecondTile() {
        return secondTile;
    }
}
