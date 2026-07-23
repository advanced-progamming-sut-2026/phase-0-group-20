package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.projectiles.FireEffect;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.enums.plants.ProjectileType;
import models.game.GameSession;

import java.util.List;

/**
 * Torchwood Strategy:
 * Passive modifier. Any friendly projectile (pea) that passes through this plant's
 * tile gets ignited: damage is boosted (x2 normally, x3 with Plant Food "blue flame")
 * and it gains the ability to melt ice / count as a fire projectile.
 */
public class TorchwoodStrategy implements IPlantStrategy {
    private boolean blueFlame = false; // set to true when boosted by Plant Food

    private boolean explodesOnDeath = false;
    private boolean hasExploded = false;

    @Override
    public void execute(Plant context, int currentTick) {
        if (explodesOnDeath && !hasExploded && context.getCurrentHp() <= 0) {
            hasExploded = true;

            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            int explosionDamage = 1800;

            notify("💥 Torchwood was destroyed and triggered a massive FIRE EXPLOSION!");

            List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 1.5f);
            for (Zombie z : targets) {
                if (!z.isDead()) {
                    z.takeDamage(explosionDamage);
                    if (z.isDead()) {
                        context.onZombieDeath(z);
                    }
                }
            }
        }
    }

    public void igniteProjectile(Projectile projectile) {
        ProjectileType type = projectile.getType();

        if (type == ProjectileType.PEA || type == ProjectileType.ICE_PEA) {

            int multiplier = blueFlame ? 3 : 2;
            int newDamage = projectile.getDamage() * multiplier;

            projectile.setType(ProjectileType.FIRE_PEA);
            projectile.setDamage(newDamage);
            projectile.setEffect(new FireEffect());

            notify("🔥 Torchwood ignited a passing projectile! Damage is now " + newDamage);
        }
    }

    public void activateBlueFlame() {
        this.blueFlame = true;
        notify("🔵 Torchwood activated Blue Flame mode!");
    }

    public void setExplodesOnDeath(boolean explodesOnDeath) {
        this.explodesOnDeath = explodesOnDeath;
    }
}
