package com.Project.PVZ.models.entities.zombies.behavior.defense;

import com.Project.PVZ.models.entities.zombies.behavior.context.SnorkelContext;
import com.Project.PVZ.models.enums.plants.ProjectileType;

public class SnorkelDefense implements DefenseBehavior {
    private final SnorkelContext context;

    public SnorkelDefense( SnorkelContext context) {
        this.context = context;
    }


    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        if (context.isSubmerged()) {
            if (!ProjectileType.isLobbed(damageType)) {
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
