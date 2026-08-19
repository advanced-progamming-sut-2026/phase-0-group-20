package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.zombies.behavior.context.ExplorerContext;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.game.GameSession;

public class ExplorerDefense implements DefenseBehavior {
    private final ExplorerContext context;

    public ExplorerDefense( ExplorerContext context) {
        this.context = context;
    }


    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (ProjectileType.isIceProjectile(damageType)) {
            if (context.isTorchLit()) {
                context.setTorchLit(false);
            }
        } else if (ProjectileType.isFireProjectile(damageType)) {
            if (!context.isTorchLit()) {
                context.setTorchLit(true);
                System.out.println("Explorer's torch was re-ignited by fire!");
            }
        }
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        return false;
    }
}
