package io.java.pvz.models.entities.zombies.behavior.defense;

import io.java.pvz.models.entities.projectiles.ProjectileType;

public class ParasolDefense implements DefenseBehavior {
    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        if (projectileType == null) return false;

        return switch (projectileType) {
            case CABBAGE, CORN, BUTTER, MELON, WINTER_MELON, PEPPER -> true;
            default -> false;
        };
    }
}
