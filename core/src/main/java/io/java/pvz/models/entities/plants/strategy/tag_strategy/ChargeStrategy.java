package io.java.pvz.models.entities.plants.strategy.tag_strategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.projectiles.*;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.plants.PlantTag;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

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

    private boolean recoveryAnimTriggered = true;
    private boolean chargeAnimTriggered = false;

    private int bowlingBulbAmmo = 1;
    private int bowlingBulbReloadStage = 0;

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
        strategy.bounceCount = 3;
        strategy.isHoming = false;
        return strategy;
    }

    @Override
    public void execute(Plant context, int currentTick) {
        if (context.getTags().contains(PlantTag.TRAP)) return;

        if (chargeStartTick == -1) {
            chargeStartTick = currentTick;
            chargeAnimTriggered = false;
            recoveryAnimTriggered = true;
            bowlingBulbAmmo = 1;
            bowlingBulbReloadStage = 0;
        }

        String name = context.getName();
        if (handleBowlingBulb(name, context)) return;

        if (name.equalsIgnoreCase("Citron")) {
            if (!recoveryAnimTriggered && context.getCurrentAction() == null) {
                context.triggerAction("recovery");
                recoveryAnimTriggered = true;
            } else if (!chargeAnimTriggered && recoveryAnimTriggered && context.getCurrentAction() == null) {
                context.triggerAction("charge");
                chargeAnimTriggered = true;
            }
        }

        int plantRow = context.getPlacedTile().getRow();
        float plantCol = context.getPlacedTile().getCol();
        int chargedTicks = currentTick - chargeStartTick;
        int requiredCharge = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);
        boolean canFire = chargedTicks >= requiredCharge;

        if (context.getCurrentAction() == null && !context.isBoosted() && !canFire) {
            if (name.equalsIgnoreCase("Caulipower")) {
                int r = random.nextInt(4) + 1;
                context.triggerAction("idle" + r + "_1");
            } else if (name.equalsIgnoreCase("Electric Blueberry")) {
                int r = random.nextInt(4) + 1;
                int sub = random.nextInt(2) + 1;
                context.triggerAction("idle" + r + "_" + sub);
            }
        }

        if (canFire && projectileType != null) {
            Zombie target = selectTarget(plantRow, plantCol, isHoming);
            if (target != null && context.getCurrentAction() == null) {
                context.triggerAction("attack");
                if (name.equalsIgnoreCase("Citron")) {
                    recoveryAnimTriggered = false;
                    chargeAnimTriggered = false;
                }
                ProjectileMechanism.executeTargetedProjectile(context, target, 0.5f);
                chargeStartTick = currentTick;
            }
        }
    }

    private Zombie selectTarget(int plantRow, float plantCol, boolean homing) {
        if (homing) {
            List<Zombie> actives = GameSession.getInstance().getArena().getActiveZombies()
                .stream()
                .filter(z -> !z.isDead() && z.getCol() < GameSession.getInstance().getArena().getCols() &&
                    !z.isHypnotized()).toList();
            if (!actives.isEmpty()) {
                return actives.get(random.nextInt(actives.size()));
            }
        } else {
            Zombie frontZombie = null;

            for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                if (!z.isDead() && z.getCol() >= plantCol && !z.isHypnotized()) {
                    if (frontZombie == null || z.getX() < frontZombie.getX()) {
                        frontZombie = z;
                    }
                }
            }

            return frontZombie;
        }
        return null;
    }

    public void speedUpRegen(float seconds) {
        this.regenSpeedup += seconds;
    }

    public void setProjectileType(ProjectileType projectileType) {
        this.projectileType = projectileType;
    }

    public void setEffect(ProjectileEffect effect) {
        this.effect = effect;
    }

    private boolean handleBowlingBulb(String name , Plant context) {
        if (name.equalsIgnoreCase("Bowling Bulb")) {
            if (context.getCurrentAction() != null) return true;

            if (bowlingBulbAmmo == 3) {
                if (bowlingBulbReloadStage == 0) {
                    context.triggerAction("reload");
                    bowlingBulbReloadStage = 1;
                } else if (bowlingBulbReloadStage == 1) {
                    context.triggerAction("reload2");
                    bowlingBulbReloadStage = 2;
                } else if (bowlingBulbReloadStage == 2) {
                    context.triggerAction("reload3");
                    bowlingBulbReloadStage = 3;
                } else if (bowlingBulbReloadStage == 3) {
                    bowlingBulbAmmo = 1;
                    bowlingBulbReloadStage = 0;
                }
                return true;
            }

            int spawnCol = context.getPlacedTile().getCol();
            int spawnRow = context.getPlacedTile().getRow();
            Zombie target = selectTarget(spawnRow, spawnCol, false);

            if (target != null) {
                int damage = 40;
                String anim = "special";
                ProjectileType pt = ProjectileType.BOWLING_BULB_CYAN;

                if (bowlingBulbAmmo == 3) {
                    damage = 180;
                    anim = "special3";
                    pt = ProjectileType.BOWLING_BULB_ORANGE;
                } else if (bowlingBulbAmmo == 2) {
                    damage = 120;
                    anim = "special2";
                    pt = ProjectileType.BOWLING_BULB_BLUE;
                }

                context.triggerAction(anim);
                bowlingBulbAmmo++;

                Projectile projectile = Projectile.spawnNewProjectile(context,
                    pt, damage, new Position(spawnCol, spawnRow),
                    0, 0, false, true
                );

                projectile.setEffect(effect);
                projectile.setHomingTarget(target, ProjectileTuning.HOMING_SPEED_TILES_PER_SEC);
                projectile.setSpawnDelayTicks(0.5f);
            }
            return true;
        }
        return false;
    }

}
