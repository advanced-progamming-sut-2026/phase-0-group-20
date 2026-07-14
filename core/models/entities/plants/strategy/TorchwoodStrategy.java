package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.projectiles.FireEffect;
import models.entities.projectiles.Projectile;
import models.enums.plants.ProjectileType;

/**
 * Torchwood Strategy:
 * Passive modifier. Any friendly projectile (pea) that passes through this plant's
 * tile gets ignited: damage is boosted (x2 normally, x3 with Plant Food "blue flame")
 * and it gains the ability to melt ice / count as a fire projectile.
 */
public class TorchwoodStrategy implements IPlantStrategy {

    private boolean blueFlame = false; // set to true when boosted by Plant Food

    @Override
    public void execute(Plant context, int currentTick) {
        // Passive: nothing happens on tick by itself.
        // Hook point: ProjectileManager should call igniteIfPassing(projectile)
        // whenever a pea-type projectile crosses this plant's tile.
    }

    public void igniteProjectile(Projectile projectile) {
        ProjectileType type = projectile.getType();

        if (type == ProjectileType.PEA || type == ProjectileType.ICE_PEA) {

            int multiplier = blueFlame ? 3 : 2;
            int newDamage = projectile.getDamage() * multiplier;

            projectile.setType(ProjectileType.FIRE_PEA);
            projectile.setDamage(newDamage);
            projectile.setEffect(new FireEffect());

            System.out.println("🔥 Torchwood ignited a passing projectile! Damage is now " + newDamage);
        }
    }

    public void activateBlueFlame() {
        this.blueFlame = true;
        System.out.println("🔵 Torchwood activated Blue Flame mode!");
    }
}
