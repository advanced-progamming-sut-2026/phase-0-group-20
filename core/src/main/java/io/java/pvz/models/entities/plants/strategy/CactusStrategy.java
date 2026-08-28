package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.Position;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.projectiles.ProjectileType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class CactusStrategy implements IPlantStrategy {
    private int lastShotTick = 0;
    private int lastMeleeTick = 0;
    private boolean isHiding = false;
    private boolean isStretching = false;
    private int pierceExtension = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        int plantRow = context.getPlacedTile().getRow();
        int plantCol = context.getPlacedTile().getCol();

        boolean[] targetStatus = scanZombiesInRow(plantRow, plantCol);
        boolean zombieOnTile = targetStatus[0];
        boolean targetInRange = targetStatus[1];
        boolean flyingTargetInRange = targetStatus[2];
        boolean boosted = context.isBoosted();

        if (handleHidingAndMelee(context, currentTick, plantRow, plantCol, zombieOnTile, boosted)) {
            return;
        }

        if (handleUnhiding(context, zombieOnTile, boosted)) {
            return;
        }

        handleStretching(context, flyingTargetInRange);
        handleShooting(context, currentTick, targetInRange, boosted);
        handleIdle(context, boosted);
    }

    private boolean[] scanZombiesInRow(int plantRow, int plantCol) {
        boolean[] status = new boolean[3]; // [zombieOnTile, targetInRange, flyingTargetInRange]

        for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
            if (z.isDead()) continue;

            boolean isFlying = z.getState() != null && z.getState().name().contains("FLY");

            if (z.getCol() == plantCol && !isFlying) {
                status[0] = true;
            }
            if (z.getCol() >= plantCol) {
                status[1] = true;
                if (isFlying) {
                    status[2] = true;
                }
            }
        }
        return status;
    }

    private boolean handleHidingAndMelee(Plant context, int currentTick, int plantRow, int plantCol,
                                         boolean zombieOnTile, boolean boosted) {
        if (!zombieOnTile) return false;

        if (!isHiding) {
            context.triggerAction(boosted ? "down_plantfood" : "down");
            isHiding = true;
            isStretching = false;
        } else if (context.getCurrentAction() == null || context.getCurrentAction().contains("down_idle")) {
            processMeleeCombat(context, currentTick, plantRow, plantCol, boosted);
        }

        return true;
    }

    private void processMeleeCombat(Plant context, int currentTick, int plantRow, int plantCol, boolean boosted) {
        if (currentTick - lastMeleeTick >= TimeManager.TICKS_PER_SECOND) {
            context.triggerAction(boosted ? "down_attack_plantfood" : "down_attack");

            for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
                if (!z.isDead() && z.getCol() == plantCol) {
                    z.takeDamage(boosted ? context.getDamage() * 2 : context.getDamage());
                }
            }
            lastMeleeTick = currentTick;
        } else if (context.getCurrentAction() == null) {
            context.triggerAction(boosted ? "down_idle_plantfood" : "down_idle");
        }
    }

    private boolean handleUnhiding(Plant context, boolean zombieOnTile, boolean boosted) {
        if (isHiding && !zombieOnTile) {
            context.triggerAction(boosted ? "up_plantfood" : "up");
            isHiding = false;
            return true;
        }
        return false;
    }

    private void handleStretching(Plant context, boolean flyingTargetInRange) {
        if (flyingTargetInRange && !isStretching) {
            context.triggerAction("up_stretch");
            isStretching = true;
        } else if (!flyingTargetInRange && isStretching) {
            context.triggerAction("down_stretch");
            isStretching = false;
        }
    }

    private void handleShooting(Plant context, int currentTick, boolean targetInRange, boolean boosted) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            if (targetInRange && context.getCurrentAction() == null) {
                if (isStretching) {
                    context.triggerAction("attack_stretch");
                } else {
                    context.triggerAction(boosted ? "attack_plantfood" : "attack");
                }
                shootPiercingProjectile(context, boosted);
                lastShotTick = currentTick;
            }
        }
    }

    private void handleIdle(Plant context, boolean boosted) {
        if (context.getCurrentAction() == null) {
            if (boosted) {
                context.triggerAction("idle_plantfood");
            }
        }
    }

    private void shootPiercingProjectile(Plant context, boolean isBoosted) {
        int plantRow = context.getPlacedTile().getRow();
        int plantCol = context.getPlacedTile().getCol();

        int damage = isBoosted ? context.getDamage() * 2 : context.getDamage();
        int pierceLimit = isBoosted ? 999 : (3 + pierceExtension);

        Projectile projectile = Projectile.spawnNewProjectile(
            context,
            ProjectileType.SPIKE,
            damage,
            new Position(plantCol, plantRow),
            ProjectileTuning.speedFor(ProjectileType.SPIKE),
            0,
            true,
            false
        );

        projectile.setSpawnDelayTicks(0.5f);
        projectile.setPierceCount(pierceLimit);
    }

    public void increasePierceLimit(int amount) {
        this.pierceExtension += amount;
    }
}
