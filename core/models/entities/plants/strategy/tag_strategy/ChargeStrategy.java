package models.entities.plants.strategy.tag_strategy;

import models.Position;
import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.projectiles.*;
import models.entities.zombies.Zombie;
import models.enums.plants.PlantTag;
import models.enums.plants.ProjectileType;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

/**
 * Charge Strategy:
 * The plant requires a long uninterrupted period to charge its attack.
 * Once fully charged, it holds the attack until a target appears.
 */

public class ChargeStrategy implements IPlantStrategy {
    private final Random random = new Random();
    private int chargeStartTick = -1;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        if (context.getTags().contains(PlantTag.TRAP)) return;

        if (chargeStartTick == -1) {
            chargeStartTick = currentTick;
            return;
        }

        String name = context.getName();
        int plantRow = context.getPlacedTile().getRow();
        float plantCol = context.getPlacedTile().getCol();

        int chargedTicks = currentTick - chargeStartTick;
        boolean canFire = false;
        int currentDamage = 0;
        ProjectileType projType = null;
        ProjectileEffect effect = new NormalEffect();
        boolean isHoming = false;

        if (name.equals("Bowling Bulb")) {
            if (chargedTicks >= 2 * TimeManager.TICKS_PER_SECOND) {
                canFire = true;
                projType = ProjectileType.PEA;

                if (chargedTicks >= 10 * TimeManager.TICKS_PER_SECOND) currentDamage = 180; // Orange Bulb
                else if (chargedTicks >= 5 * TimeManager.TICKS_PER_SECOND) currentDamage = 120; // Blue Bulb
                else currentDamage = 40; // Cyan Bulb
            }
        } else {
            int requiredCharge = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

            if (chargedTicks >= requiredCharge) {
                canFire = true;

                if (name.equals("Citron")) {
                    projType = ProjectileType.PEA;
                    currentDamage = 800;
                } else if (name.equals("Caulipower")) {
                    projType = ProjectileType.MAGIC_BEAM;
                    currentDamage = 0;
                    effect = new HypnotizeEffect();
                    isHoming = true;
                } else if (name.equals("Electric Blueberry")) {
                    projType = ProjectileType.LIGHTNING_CLOUD;
                    currentDamage = 5000;
                    effect = new LightningEffect();
                    isHoming = true;
                }
            }
        }

        if (canFire) {
            Zombie target = null;

            if (isHoming) {
                List<Zombie> actives = gameSession.getArena().getActiveZombies().stream().filter(z -> !z.isDead()).toList();
                if (!actives.isEmpty()) {
                    target = actives.get(random.nextInt(actives.size()));
                }
            } else {
                for (Zombie z : gameSession.getArena().zombieInRow(plantRow)) {
                    if (!z.isDead() && z.getX() >= plantCol) {
                        target = z;
                        break;
                    }
                }
            }

            if (target != null && projType != null) {
                Projectile projectile = new Projectile(
                        context,
                        projType, effect, gameSession, currentDamage,
                        new Position(plantCol, plantRow),
                        isHoming ? 0 : 1.5f,
                        0,
                        false, isHoming
                );

                if (isHoming) {
                    projectile.setHomingTarget(target, 0.8f);
                    System.out.println("🔮 " + name + " fired a fully charged homing attack at " + target.getName() + "!");
                } else {
                    System.out.println("🔋 " + name + " fired a charged attack! (Damage: " + currentDamage + ")");

                    if (name.equals("Bowling Bulb")) {
                        projectile.setBouncesLeft(3);
                        System.out.println("🎳 Bowling Bulb fired a bouncing bulb!");
                    }
                }

                gameSession.addProjectile(projectile);
                chargeStartTick = currentTick;
            }
        }
    }
}
