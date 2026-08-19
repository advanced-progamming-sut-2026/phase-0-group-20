package io.java.pvz.models.entities.plants.strategy.category_strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

public class MeleeStrategy implements IPlantStrategy {
    private int lastAttackTick = 0;
    private int aliveTicks = 0;
    private float rangeExtension = 0;
    private final Random rand = new Random();

    private int attackCount = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        aliveTicks++;
        String name = context.getName();

        if (name.equals("Chomper")) return;

        if (name.equals("Kiwibeast")) {
            handleKiwibeastState(context);
        }

        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastAttackTick) >= intervalInTicks) {

            if (name.equals("Kiwibeast") && context.getCurrentAction() != null &&
                (context.getCurrentAction().contains("growth") || context.getCurrentAction().contains("plantfood"))) {
                return;
            }

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

    private void handleKiwibeastState(Plant context) {
        int stage = context.getSize();
        int secondsAlive = aliveTicks / TimeManager.TICKS_PER_SECOND;

        if (context.isBoosted()) {
            if (stage < 3) {
                context.setSize(3);
            }
            if (context.getCurrentAction() == null || !context.getCurrentAction().contains("plantfood")) {
                context.triggerAction("plantfood_stage3");
            }
        } else {
            if (stage == 1 && secondsAlive >= 24) {
                context.setSize(2);
                context.triggerAction(rand.nextBoolean() ? "growth_stage1" : "growth_stage1_2");
            } else if (stage == 2 && secondsAlive >= 72) {
                context.setSize(3);
                context.triggerAction("growth_stage2");
            }
        }

        if (context.getCurrentAction() == null && !context.isBoosted()) {
            int r = rand.nextInt(3) + 1;
            String idleAnim = (r == 1) ? "idle_stage" + context.getSize() + "_" : "idle_stage" + context.getSize() + "_" + r;
            context.triggerAction(idleAnim);
        }
    }

    private boolean executeSingleTargetMelee(String name, Plant context, int plantRow, int plantCol, int baseDamage) {
        Zombie target = null;
        double minDistance = Double.MAX_VALUE;

        double forwardRange = 1.5 + rangeExtension;
        double backwardRange = -1.0;

        for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
            if (z.isDead()) continue;
            double dist = (z.getX() - PhysicalConstants.GRID_START_X) / PhysicalConstants.TILE_WIDTH  - plantCol;

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

            int r = rand.nextInt(5) + 1;
            String animName = r == 1 ? "attack" : "attack" + r;
            context.triggerAction(animName);

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
                int stage = context.getSize();
                if (stage == 3) {
                    finalDamage = baseDamage * 3;
                } else if (stage == 2) {
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
                if (name.equals("Kiwibeast")) {
                    context.triggerAction("attack_stage" + context.getSize());
                } else {
                    context.triggerAction("attack");
                }
                notify("🔊 " + name + " slammed a 3x3 area, hitting " + targets.size() + " zombies!");
            }
        }

        return attacked;
    }

    public void increaseRange(float range) {
        this.rangeExtension += range;
    }
}
