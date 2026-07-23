package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.zombies.Zombie;
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

    private int extraGrabTargets = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        String name = context.getName();

        if (!initialized) {
            armingTimeTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);
            initialized = true;
        }
        if (startTick == -1) startTick = currentTick;

        if (!isArmed && (currentTick - startTick) >= armingTimeTicks) {
            isArmed = true;
            if (armingTimeTicks > 0) {
                notify("💣 " + name + " is now armed and ready!");
            }
        }

        if (!isArmed) return;

        int plantRow = context.getPlacedTile().getRow();
        double plantCol = context.getPlacedTile().getCol();
        List<Zombie> targets = new ArrayList<>();
        double detectionRadius = name.equals("Squash") ? 1.5 : 0.5;

        int maxTargetsAllowed = name.equals("Tangle Kelp") ? (1 + extraGrabTargets) : 1;

        for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
            if (z.isDead()) continue;

            double dist = Math.abs(z.getX() / PhysicalConstants.TILE_UNIT_LENGTH - plantCol);
            if (dist <= detectionRadius) {
                targets.add(z);
                if (targets.size() >= maxTargetsAllowed) {
                    break;
                }
            }
        }

        if (!targets.isEmpty()) {
            notify("🚨 " + name + " TRAP TRIGGERED!");
            boolean killed;

            boolean shouldDie = true;

            switch (name) {
                case "Potato Mine":
                    Zombie pmTarget = targets.get(0);
                    killed = pmTarget.takeDirectDamage(1800);
                    if (killed) context.onZombieDeath(pmTarget);
                    break;

                case "Primal Potato Mine":
                    List<Zombie> aoeTargets = GameSession.getInstance().getArena().getZombiesInRadius((int) plantCol, plantRow, 1.5);
                    for (Zombie z : aoeTargets) {
                        if (!z.isDead()) {
                            killed = z.takeDirectDamage(2400);
                            if (killed) context.onZombieDeath(z);
                        }
                    }
                    notify("💥 Primal Potato Mine dealt massive AoE damage!");
                    break;

                case "Squash":
                    Zombie squashTarget = targets.get(0);
                    killed = squashTarget.takeDirectDamage(1800);
                    if (killed) context.onZombieDeath(squashTarget);
                    notify("🪨 Squash crushed " + squashTarget.getName() + "!");

                    smashCount++;
                    int totalAllowedSmashes = 1 + extraSmashCharges;
                    if (smashCount < totalAllowedSmashes) {
                        shouldDie = false;
                    }
                    break;

                case "Tangle Kelp":
                    for (Zombie z : targets) {
                        killed = z.takeDirectDamage(9999);
                        if (killed) context.onZombieDeath(z);
                        notify("🌊 Tangle Kelp pulled " + z.getName() + " underwater!");
                    }
                    break;

                case "Iceberg Lettuce":
                    Zombie iceTarget = targets.get(0);
                    notify("❄️ Iceberg Lettuce completely froze " + iceTarget.getName() + "!");
                    break;
            }

            if (shouldDie) {
                context.takeDamage(context.getCurrentHp());
            }
        }

    }

    public int getArmingTimeTicks() {
        return armingTimeTicks;
    }

    public void setArmingTimeTicks(int armingTime) {
        this.armingTimeTicks = armingTime;
    }

    public void setArmed(boolean armed) {
        this.isArmed = armed;
    }

    public void increaseFreezeDuration(float value) {

    }

    public void increaseSmashCharges(int amount) {
        this.extraSmashCharges += amount;
    }

    public void increaseMaxTargets(int amount) {
        this.extraGrabTargets += amount;
    }

}
