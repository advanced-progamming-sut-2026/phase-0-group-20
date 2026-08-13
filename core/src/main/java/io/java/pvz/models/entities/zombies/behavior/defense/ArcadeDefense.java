package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.obstacle.ArcadeMachine;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.move.ArcadeMove;
import io.java.pvz.models.enums.plants.ProjectileType;

public class ArcadeDefense implements DefenseBehavior {
    private final Zombie zombie;

    public ArcadeDefense(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (zombie.getMoveBehavior() instanceof ArcadeMove moveBehavior) {
            ArcadeMachine machine = moveBehavior.getArcadeMachine();
            if (machine != null && !machine.isDestroyed()) {
                machine.takeDamage(damage);
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
