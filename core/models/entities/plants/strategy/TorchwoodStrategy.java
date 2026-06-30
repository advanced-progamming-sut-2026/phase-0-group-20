package models.entities.plants.strategy;

import models.entities.plants.Plant;

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

    public void igniteProjectile(/* Projectile projectile */) {
        int multiplier = blueFlame ? 3 : 2;
        System.out.println("Torchwood ignited a passing projectile! Damage x" + multiplier);
        // Logic: projectile.setDamage(projectile.getDamage() * multiplier); projectile.setFire(true);
    }

    public void activateBlueFlame() {
        this.blueFlame = true;
    }
}
