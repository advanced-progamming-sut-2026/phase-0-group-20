package models.entities.zombies.behavior.defense;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.context.ProspectorContext;
import models.enums.plants.ProjectileType;

public class ProspectorDefense implements DefenseBehavior {
    private final Zombie zombie;
    private final ProspectorContext context;

    public ProspectorDefense(Zombie zombie, ProspectorContext context) {
        this.zombie = zombie;
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
