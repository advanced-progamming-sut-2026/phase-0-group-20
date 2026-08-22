package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.obstacle.Barrel;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.move.BarrelRollerMove;
import io.java.pvz.models.entities.projectiles.ProjectileType;

public class BarrelRollerDefense implements DefenseBehavior {
    private final Zombie zombie;

    public BarrelRollerDefense(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (zombie.getMoveBehavior() instanceof BarrelRollerMove moveBehavior) {
            Barrel barrel = moveBehavior.getBarrel();
            if (barrel != null && !barrel.isDestroyed()) {
                barrel.takeDamage(damage);
                return 0;
            }
        }
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        return false;
    }
}
