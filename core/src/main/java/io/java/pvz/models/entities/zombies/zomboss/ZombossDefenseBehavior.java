package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.behavior.defense.DefenseBehavior;
import io.java.pvz.models.enums.plants.ProjectileType;

public class ZombossDefenseBehavior implements DefenseBehavior {
    private final Zomboss zomboss;

    public ZombossDefenseBehavior(Zomboss zomboss) {
        this.zomboss = zomboss;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {

        return false;
    }

    @Override
    public int mitigateDamage(int damage, ProjectileType projectileType) {
        if (zomboss.isDead()) {
            return 0;
        }

        if (zomboss.getState() == ZombieState.STUNNED) {
            return 0;
        }

        int currentPhaseHp = zomboss.getCurrentPhaseHealth();

        if (damage >= currentPhaseHp) {
            if (zomboss.getPhase() > 1) {
                int allowedDamage = currentPhaseHp;

                zomboss.triggerStun();

                return allowedDamage;
            } else {
                zomboss.reducePhaseHealth(damage);
                return damage;
            }
        }

        zomboss.reducePhaseHealth(damage);
        return damage;
    }
}
