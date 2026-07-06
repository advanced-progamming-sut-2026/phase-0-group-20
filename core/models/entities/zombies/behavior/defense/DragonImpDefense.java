package models.entities.zombies.behavior.defense;

import models.enums.plants.ProjectileType;

public class DragonImpDefense implements DefenseBehavior {
    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (damageType == ProjectileType.FIRE_PEA) {
            return 0;
        }
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        return false;
    }
}
