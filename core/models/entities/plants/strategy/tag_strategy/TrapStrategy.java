package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.effect.ChillEffect;
import models.enums.PhysicalConstants;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;


/**
 * Trap Strategy:
 * The plant remains inactive (unarmed) for a specific duration after planting.
 * Once armed, it monitors its tile (or adjacent ones) and triggers a deadly effect
 * when an enemy steps on it.
 */

public class TrapStrategy implements IPlantStrategy {
    private int startTick = -1;
    private boolean isArmed = false;
    private boolean initialized = false;
    private int armingTimeTicks = 0;

    private int extraSmashCharges = 0;
    private int smashCount = 0;

    private int lastAttackTick = 0;

    private int extraGrabTargets = 0;
    private float freezeDurationBonus = 0f;

    @Override
    public void execute(Plant context, int currentTick) {
        String name = context.getName();

        if (!handleArming(context, currentTick, name)) {
            return;
        }

        int plantRow = context.getPlacedTile().getRow();
        double plantCol = context.getPlacedTile().getCol();

        List<Zombie> targets = findTargets(name, plantRow, plantCol);

        if (!targets.isEmpty()) {
            notify("🚨 " + name + " TRAP TRIGGERED!");
            int baseDamage = context.getDamage() > 0 ? context.getDamage() : 1800;

            boolean shouldDie = executeTrapEffect(
                    name, context, targets, baseDamage, currentTick, plantCol, plantRow
            );

            if (shouldDie) {
                context.takeDamage(context.getCurrentHp());
            }
        }
    }

    private boolean handleArming(Plant context, int currentTick, String name) {
        if (!initialized) {
            armingTimeTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);
            if (armingTimeTicks <= 0) {
                isArmed = true;
            }
            initialized = true;
        }

        if (startTick == -1) startTick = currentTick;

        if (!isArmed) {
            if ((currentTick - startTick) >= armingTimeTicks) {
                isArmed = true;
                notify("💣 " + name + " is now armed and ready!");
            } else {
                return false;
            }
        }
        return true;
    }

    private List<Zombie> findTargets(String name, int plantRow, double plantCol) {
        List<Zombie> targets = new ArrayList<>();
        double detectionRadius = name.equals("Squash") ? 1.5 : 0.5;
        int maxTargetsAllowed = name.equals("Tangle Kelp") ? (1 + extraGrabTargets) : 1;

        for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
            if (z.isDead()) continue;

            double zColFloat = z.getX() / PhysicalConstants.TILE_UNIT_LENGTH;
            double dist = Math.abs(zColFloat - plantCol);

            if (dist <= detectionRadius) {
                targets.add(z);
                if (targets.size() >= maxTargetsAllowed) {
                    break;
                }
            }
        }
        return targets;
    }

    private boolean executeTrapEffect(String name, Plant context, List<Zombie> targets,
                                      int baseDamage, int currentTick, double plantCol, int plantRow) {
        boolean shouldDie = true;

        switch (name) {
            case "Potato Mine" -> {
                Zombie pmTarget = targets.getFirst();
                pmTarget.takeDamage(baseDamage);
                if (pmTarget.isDead()) context.onZombieDeath(pmTarget);
            }
            case "Primal Potato Mine" -> {
                List<Zombie> aoeTargets = GameSession.getInstance().getArena()
                        .getZombiesInRadius((int) plantCol, plantRow, 1.5);
                for (Zombie z : aoeTargets) {
                    if (!z.isDead()) {
                        z.takeDamage(Math.max(baseDamage, 2400));
                        if (z.isDead()) context.onZombieDeath(z);
                    }
                }
                notify("💥 Primal Potato Mine dealt massive AoE damage!");
            }
            case "Squash" -> {
                Zombie squashTarget = targets.getFirst();
                squashTarget.takeDamage(baseDamage);
                if (squashTarget.isDead()) context.onZombieDeath(squashTarget);
                notify("🪨 Squash crushed " + squashTarget.getName() + "!");

                smashCount++;
                int totalAllowedSmashes = 1 + extraSmashCharges;
                if (smashCount < totalAllowedSmashes) {
                    shouldDie = false;
                    lastAttackTick = currentTick;
                }
            }
            case "Tangle Kelp" -> {
                for (Zombie z : targets) {
                    z.takeDamage(9999);
                    if (z.isDead()) context.onZombieDeath(z);
                    notify("🌊 Tangle Kelp pulled " + z.getName() + " underwater!");
                }
            }
            case "Iceberg Lettuce" -> {
                Zombie iceTarget = targets.getFirst();
                iceTarget.addEffect(new ChillEffect(iceTarget, (int) (10 + freezeDurationBonus)));
                notify("❄️ Iceberg Lettuce completely froze " + iceTarget.getName() + "!");
            }
        }

        return shouldDie;
    }


    public void setArmingTimeTicks(int armingTime) {
        this.armingTimeTicks = armingTime;
    }

    public int getArmingTimeTicks() {
        return armingTimeTicks;
    }

    public void setArmed(boolean armed) {
        this.isArmed = armed;
    }

    public void increaseFreezeDuration(float value) {
    }

    public boolean isArmed() {
        return isArmed;
    }

    public void increaseSmashCharges(int amount) {
        this.extraSmashCharges += amount;
    }

    public void increaseMaxTargets(int amount) {
        this.extraGrabTargets += amount;
    }

}
