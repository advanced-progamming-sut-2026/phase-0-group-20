package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;

public class MeleeStrategy implements IPlantStrategy {
    private int lastAttackTick = 0;
    private int aliveTicks = 0;
    private float rangeExtension = 0;

    private int attackCount = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        aliveTicks++;
        String name = context.getName();

        if (name.equals("Chomper")) return;

        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastAttackTick) >= intervalInTicks) {
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            boolean attacked = false;

            int baseDamage = context.getDamage() > 0 ? context.getDamage() : 15;

            if (name.equals("Bonk Choy") || name.equals("Wasabi Whip")) {
                attacked = executeSingleTargetMelee(name, context, plantRow, plantCol, baseDamage);
            } else if (name.equals("Phat Beet") || name.equals("Kiwibeast")) {
                attacked = executeAoEMelee(name, context, plantRow, plantCol, baseDamage);
            }

            if (attacked) {
                lastAttackTick = currentTick;
                attackCount++;
            }
        }
    }

    private boolean executeSingleTargetMelee(String name, Plant context, int plantRow, int plantCol, int baseDamage) {
        Zombie target = null;
        double minDistance = Double.MAX_VALUE;

        double forwardRange = 1.5 + rangeExtension;
        double backwardRange = -1.0;

        for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
            if (z.isDead()) continue;
            double dist = z.getX() / PhysicalConstants.TILE_UNIT_LENGTH - plantCol;

            if (dist >= backwardRange && dist <= forwardRange) {
                if (Math.abs(dist) < minDistance) {
                    minDistance = Math.abs(dist);
                    target = z;
                }
            }
        }

        if (target != null) {
            int damage = name.equals("Wasabi Whip") ? Math.max(baseDamage, 40) : baseDamage;

            target.takeDamage(damage);
            if (target.isDead()) {
                context.onZombieDeath(target);
            }

            if (name.equals("Wasabi Whip")) {
                target.removeChillEffect();
                target.removeFreezeEffect();
            }
            notify("🥊 " + name + " landed a melee strike on " + target.getName() + "!");
            return true;
        }

        return false;
    }

    private boolean executeAoEMelee(String name, Plant context, int plantRow, int plantCol, int baseDamage) {
        List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 1.5);
        boolean attacked = false;

        if (!targets.isEmpty()) {
            int finalDamage = baseDamage;

            if (name.equals("Kiwibeast")) {
                int secondsAlive = aliveTicks / TimeManager.TICKS_PER_SECOND;
                if (secondsAlive >= 72) {
                    finalDamage = baseDamage * 3;
                } else if (secondsAlive >= 24) {
                    finalDamage = 30;
                }
            } else {
                if (attackCount % 4 == 3) {
                    finalDamage = baseDamage * 3;
                    notify("💥 Phat Beet landed a CRITICAL thump!");
                }
            }

            for (Zombie z : targets) {
                if (!z.isDead()) {
                    z.takeDamage(finalDamage);
                    if (z.isDead()) {
                        context.onZombieDeath(z);
                    }
                    attacked = true;
                }
            }

            if (attacked) {
                notify("🔊 " + name + " slammed a 3x3 area, hitting " + targets.size() + " zombies!");
            }
        }

        return attacked;
    }

    public void increaseRange(float range) {
        this.rangeExtension += range;
    }
}
