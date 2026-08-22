package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.zombies.behavior.context.ProspectorContext;
import io.java.pvz.models.entities.projectiles.ProjectileType;

public class ProspectorDefense implements DefenseBehavior {
    private final ProspectorContext context;

    public ProspectorDefense( ProspectorContext context) {
        this.context = context;
    }

    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (context.isDynamiteLit() && ProjectileType.isIceProjectile(damageType)) {
            context.extinguishDynamite();
        }

        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        return false;
    }
}
