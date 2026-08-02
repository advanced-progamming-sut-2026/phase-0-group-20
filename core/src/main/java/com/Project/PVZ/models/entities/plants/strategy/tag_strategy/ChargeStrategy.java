package com.Project.PVZ.models.entities.plants.strategy.tag_strategy;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.plants.strategy.IPlantStrategy;
import com.Project.PVZ.models.entities.projectiles.*;
import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.enums.plants.PlantTag;
import com.Project.PVZ.models.enums.plants.ProjectileType;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

public class ChargeStrategy implements IPlantStrategy {
    private final Random random = new Random();
    private int chargeStartTick = -1;
    private float regenSpeedup = 0;
    private ProjectileType projectileType;
    private ProjectileEffect effect;
    private int baseDamage;
    private boolean isHoming;
    private int bounceCount;
    private boolean isMultiStage;

    public ChargeStrategy() {
        this.effect = new NormalEffect();
        this.isHoming = false;
        this.bounceCount = 0;
        this.isMultiStage = false;
    }

    public ChargeStrategy(ProjectileType projectileType, ProjectileEffect effect, int baseDamage, boolean isHoming) {
        this.projectileType = projectileType;
        this.effect = effect != null ? effect : new NormalEffect();
        this.baseDamage = baseDamage;
        this.isHoming = isHoming;
        this.bounceCount = 0;
        this.isMultiStage = false;
    }

    public static ChargeStrategy createBowlingBulbStrategy() {
        ChargeStrategy strategy = new ChargeStrategy();
        strategy.isMultiStage = true;
        strategy.projectileType = ProjectileType.PEA;
        strategy.bounceCount = 3;
        strategy.isHoming = false;
        return strategy;
    }

    @Override
    public void execute(Plant context, int currentTick) {
        if (context.getTags().contains(PlantTag.TRAP)) return;
        if (chargeStartTick == -1) {
            chargeStartTick = currentTick;
            return;
        }
        int plantRow = context.getPlacedTile().getRow();
        float plantCol = context.getPlacedTile().getCol();
        int chargedTicks = currentTick - chargeStartTick;
        boolean canFire = false;
        int currentDamage = this.baseDamage;
        ProjectileType projType = this.projectileType;
        boolean homingAttack = this.isHoming;
        if (isMultiStage) {
            float cyanTime = Math.max(0, 2.0f - regenSpeedup);
            float blueTime = Math.max(0, 5.0f - regenSpeedup);
            float orangeTime = Math.max(0, 10.0f - regenSpeedup);

            if (chargedTicks >= cyanTime * TimeManager.TICKS_PER_SECOND) {
                canFire = true;
                projType = ProjectileType.PEA;

                if (chargedTicks >= orangeTime * TimeManager.TICKS_PER_SECOND) currentDamage = 180; // Orange Bulb
                else if (chargedTicks >= blueTime * TimeManager.TICKS_PER_SECOND) currentDamage = 120; // Blue Bulb
                else currentDamage = 40;  // Cyan Bulb
            }
        } else {
            int requiredCharge = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);
            if (chargedTicks >= requiredCharge) {canFire = true;}
        }
        if (canFire && projType != null) {
            Zombie target = selectTarget(plantRow, plantCol, homingAttack);
            if (target != null) {
                if (homingAttack) {
                    ProjectileMechanism.executeTargetedProjectile(context, target, 0);
                    notify("🔮 " + context.getName() + " fired a fully charged homing attack at "
                            + target.getName() + "!");
                } else {
                    notify("🔋 " + context.getName() + " fired a charged attack! (Damage: " + currentDamage + ")");
                    ProjectileMechanism.executeTargetedProjectile(context, target, 0);
                    if (bounceCount > 0) {
//                        projectile.setBouncesLeft(bounceCount);
                        notify("🎳 " + context.getName() + " fired a bouncing bulb!");
                    }
                }
                chargeStartTick = currentTick;
            }
        }
    }

    private Zombie selectTarget(int plantRow, float plantCol, boolean homing) {
        if (homing) {
            List<Zombie> actives = GameSession.getInstance().getArena().getActiveZombies()
                    .stream()
                    .filter(z -> !z.isDead()).toList();
            if (!actives.isEmpty()) {
                return actives.get(random.nextInt(actives.size()));
            }
        } else {
            for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                if (!z.isDead() && z.getCol() >= plantCol) {
                    return z;
                }
            }
        }
        return null;
    }

    public void speedUpRegen(float seconds) {this.regenSpeedup += seconds;}
    public void setProjectileType(ProjectileType projectileType) {this.projectileType = projectileType;}
    public void setEffect(ProjectileEffect effect) {this.effect = effect;}

}
