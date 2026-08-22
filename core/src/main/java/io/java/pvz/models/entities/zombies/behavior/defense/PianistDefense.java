package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.obstacle.Piano;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.move.PianistMove;
import io.java.pvz.models.entities.projectiles.ProjectileType;

public class PianistDefense implements DefenseBehavior {
    private final Zombie zombie;

    public PianistDefense(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (zombie.getMoveBehavior() instanceof PianistMove moveBehavior) {
            Piano piano = moveBehavior.getPiano();
            if (piano != null && !piano.isDestroyed()) {
                piano.takeDamage(damage);
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
