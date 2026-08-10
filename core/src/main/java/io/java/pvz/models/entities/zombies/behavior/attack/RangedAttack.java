package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.plants.ProjectileType;

public class RangedAttack implements AttackBehavior {

    private final static boolean DEF_PIER = false;
    private final static boolean DEF_C_P_O = false; //can pass obstacles

    private final Zombie zombie;
    private final ProjectileType projectileType;
    private final int damage;


    public RangedAttack(Zombie zombie,
                        ProjectileType projectileType,
                        int damage) {
        this.zombie = zombie;
        this.projectileType = projectileType;
        this.damage = damage;
    }

    @Override
    public void execute() {
        if (zombie.isDead()) return;

        Position spawnPosition = new Position(zombie.getCol(), zombie.getRow());
        float speed = ProjectileTuning.speedFor(projectileType);

        Projectile.spawnZombieProjectile(
            zombie,
            projectileType,
            damage,
            spawnPosition,
            -speed, // zombies are on the right, firing leftward at the plants
            0,
            DEF_PIER,
            DEF_C_P_O
        );
    }
}
