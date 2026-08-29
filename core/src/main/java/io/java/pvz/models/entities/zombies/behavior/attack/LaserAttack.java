package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.behavior.context.TurquoiseContext;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

public class LaserAttack implements AttackBehavior {
    private final Zombie zombie;
    private final TurquoiseContext context;
    private final AttackBehavior normalAttack;

    private static final int STEAL_AMOUNT = 25;
    private static final int LASER_DAMAGE = 4000;

    private final int powerUpTicks;
    private final int powerTicks;
    private final int attackTicks;
    private final int powerDownTicks;

    private boolean hasFired = false;

    public LaserAttack(Zombie zombie, TurquoiseContext context) {
        this.zombie = zombie;
        this.context = context;
        this.normalAttack = new NormalAttack(zombie);
        this.powerUpTicks = (int) (0.6667f * TimeManager.TICKS_PER_SECOND);
        this.powerTicks = (int) (2.5f * TimeManager.TICKS_PER_SECOND);
        this.attackTicks = (int) (1.9667f * TimeManager.TICKS_PER_SECOND);
        this.powerDownTicks = (int) (1.2667f * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void execute() {
        if (!context.isCharging()) {
            Tile currentTile = GameSession.getInstance().getArena().getTile(zombie.getRow(), zombie.getCol());
            if (currentTile != null && !currentTile.getPlants().isEmpty()) {
                zombie.setAttacking(true);
                normalAttack.execute();
                return;
            }
            if (GameSession.getInstance().getCurrentSun() < STEAL_AMOUNT) {
                zombie.setState(ZombieState.WALKING);
                zombie.resetSpeed();
                return;
            }
            context.startCharging();
            hasFired = false;
            notify("Crystal Skull Zombie is warming up its laser!");
        }
        context.incrementCharge();
        int ticks = context.getChargeTicks();
        zombie.applySpeedMultiplier(0f);
        if (ticks <= powerUpTicks) zombie.setState(ZombieState.POWER_UP);
        else if (ticks <= powerUpTicks + powerTicks) {
            zombie.setState(ZombieState.POWER);
            if (ticks % (TimeManager.TICKS_PER_SECOND / 2) == 0) {
                stealSun(25);
            }
        }

        else if (ticks <= powerUpTicks + powerTicks + attackTicks) {
            zombie.setState(ZombieState.SPECIAL);

            int fireTickThreshold = powerUpTicks + powerTicks + (int) (attackTicks * 0.3f);
            if (ticks >= fireTickThreshold && !hasFired) {
                fireLaser();
                hasFired = true;
            }
        }

        else if (ticks <= powerUpTicks + powerTicks + attackTicks + powerDownTicks) {
            zombie.setState(ZombieState.POWER_DOWN);
        }

        else {
            context.reset();
            hasFired = false;
            zombie.setState(ZombieState.WALKING);
            zombie.setAttacking(false);
            zombie.resetSpeed();
        }
    }

    private void stealSun(int targetAmount) {
        int currentBank = GameSession.getInstance().getCurrentSun();

        if (currentBank >= targetAmount) {
            GameSession.getInstance().useSun(targetAmount);
            context.addStolenSun(targetAmount);
            notify("Crystal Skull Zombie stole " + targetAmount + " suns!");
        }
    }

    private void fireLaser() {
        GameSession session = GameSession.getInstance();
        int zRow = zombie.getRow();
        int zCol = zombie.getCol();

        List<Plant> targets = new ArrayList<>();
        for (Plant p : session.getArena().getActivePlants()) {
            int pRow = p.getPlacedTile().getRow();
            int pCol = p.getPlacedTile().getCol();
            if (pRow == zRow && pCol <= zCol && pCol >= zCol - 4) {
                targets.add(p);
            }
        }

        for (Plant p : targets) {
            p.takeDamage(LASER_DAMAGE);
            if (p.isDead()) {
                p.getPlacedTile().getPlants().remove(p);
            }
        }

        notify("Crystal Skull Zombie fired a deadly laser!");

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("CRYSTAL_SKULL_BEAM")
                .zombie(zombie)
                .build());
    }
}
