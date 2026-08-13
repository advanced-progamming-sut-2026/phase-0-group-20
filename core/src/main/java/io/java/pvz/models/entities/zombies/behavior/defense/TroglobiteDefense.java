package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.obstacle.IceBlock;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.move.TroglobiteMove;
import io.java.pvz.models.enums.plants.ProjectileType;

public class TroglobiteDefense implements DefenseBehavior {
    private final Zombie zombie;

    public TroglobiteDefense(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (zombie.getMoveBehavior() instanceof TroglobiteMove moveBehavior) {
            IceBlock iceBlock = moveBehavior.getCurrentTargetIceBlock();
            if (iceBlock != null && !iceBlock.isDestroyed()) {
                iceBlock.takeDamage(damage);
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
