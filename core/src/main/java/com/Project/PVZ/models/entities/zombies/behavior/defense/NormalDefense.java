package com.Project.PVZ.models.entities.zombies.behavior.defense;

import com.Project.PVZ.models.enums.plants.ProjectileType;

public class NormalDefense implements DefenseBehavior {
    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        return false;
    }
}
