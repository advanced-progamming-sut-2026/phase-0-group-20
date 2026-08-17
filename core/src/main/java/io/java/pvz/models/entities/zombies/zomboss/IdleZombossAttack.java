package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

public class IdleZombossAttack implements IZombossAttack {
    private final Zomboss zomboss;
    private final List<IZombossAttack> availableAttacks;
    private final Random random = new Random();
    private int cooldownTicks;

    public IdleZombossAttack(Zomboss zomboss, List<IZombossAttack> availableAttacks) {
        this.zomboss = zomboss;
        this.availableAttacks = availableAttacks;
    }

    @Override
    public void onEnter() {
        this.cooldownTicks = (4 + random.nextInt(5)) * TimeManager.TICKS_PER_SECOND;
        zomboss.setState(ZombieState.BOSS_IDLE);

    }

    @Override
    public void execute() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
        } else {
            if (!availableAttacks.isEmpty()) {
                IZombossAttack nextAttack = availableAttacks.get(random.nextInt(availableAttacks.size()));

                this.onExit();
                nextAttack.onEnter();

                zomboss.setAttackBehavior(nextAttack);
            }
        }
    }

    @Override
    public void onExit() {
    }

    public void reset() {
        this.cooldownTicks = 0;
    }
}
