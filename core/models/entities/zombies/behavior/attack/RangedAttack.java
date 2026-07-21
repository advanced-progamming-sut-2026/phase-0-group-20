package models.entities.zombies.behavior.attack;

import models.Position;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;

public class RangedAttack implements AttackBehavior {

    private final static int SPEED_X = -1;
    private final static int SPEED_Y = 0;
    private final static boolean DEF_PIER = false;
    private final static boolean DEF_C_P_O = false; //can pass obstacles

    private final Zombie zombie;
    private final ProjectileType projectileType;
    private final int damage;


    public RangedAttack(Zombie zombie,
                        ProjectileType projectileType,
                        int damage){
        this.zombie = zombie;
        this.projectileType = projectileType;
        this.damage = damage;
    }

    @Override
    public void execute() {
        if (zombie.isDead()) return;

        Position spawnPosition = new Position(zombie.getCol(), zombie.getRow());

        Projectile.spawnZombieProjectile(
                zombie,
                projectileType,
                damage,
                spawnPosition,
                SPEED_X,
                SPEED_Y,
                DEF_PIER,
                DEF_C_P_O
        );
    }
}