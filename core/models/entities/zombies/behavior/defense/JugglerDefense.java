package models.entities.zombies.behavior.defense;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.move.JugglerMove;
import models.enums.plants.ProjectileType;

public class JugglerDefense implements DefenseBehavior {

    private final Zombie zombie;

    public JugglerDefense(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public int mitigateDamage(int damage, ProjectileType damageType) {
        return damage;
    }

    @Override
    public boolean deflectProjectile(ProjectileType projectileType) {
        try {
            return switch (projectileType) {
                case PEA, ICE_PEA, ROTOBAGA_SEED, FIRE_PEA, GOO_PEA,
                     CABBAGE, CORN, BUTTER, MELON, WINTER_MELON,
                     PEPPER, GRAPE, SPIKE -> true;
                case MAGIC_BEAM, LIGHTNING_CLOUD, FUME -> false;
            };
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
