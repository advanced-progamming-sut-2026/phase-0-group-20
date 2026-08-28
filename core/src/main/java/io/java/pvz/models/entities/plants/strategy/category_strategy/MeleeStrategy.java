package io.java.pvz.models.entities.plants.strategy.category_strategy;

import io.java.pvz.models.entities.obstacle.GraveHolder;
import io.java.pvz.models.entities.obstacle.IceHolder;
import io.java.pvz.models.entities.obstacle.PushableObstacle;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.models.fields.tiles.Tile;

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
            String idleAnim = (r == 1) ? "idle_stage" + context.getSize() + "_" :
                "idle_stage" + context.getSize() + "_" + r;
            context.triggerAction(idleAnim);
        }
    }

    private boolean executeSingleTargetMelee(String name, Plant context, int plantRow, int plantCol, int baseDamage) {
        double forwardRange = 1.5 + rangeExtension;
        double backwardRange = -1.0;
        int damage = name.equals("Wasabi Whip") ? Math.max(baseDamage, 40) : baseDamage;

        Zombie targetZombie = findSingleMeleeZombie(plantRow, plantCol, forwardRange, backwardRange);
        if (targetZombie != null) {
            return applyMeleeToZombie(name, context, targetZombie, damage);
        }

        Object targetObstacle = findSingleMeleeObstacle(plantRow, plantCol, forwardRange, backwardRange);
        if (targetObstacle != null) {
            return applyMeleeToObstacle(name, context, targetObstacle, plantRow, damage);
        }

        return false;
    }

    private Zombie findSingleMeleeZombie(int plantRow, int plantCol, double fRange, double bRange) {
        Zombie target = null;
        double minDistance = Double.MAX_VALUE;

        for (Zombie z : GameSession.getInstance().getArena().zombieInRow(plantRow)) {
            if (z.isDead()) continue;
            double dist = (z.getX() - PhysicalConstants.GRID_START_X) / PhysicalConstants.TILE_WIDTH - plantCol;

            if (dist >= bRange && dist <= fRange && Math.abs(dist) < minDistance) {
                minDistance = Math.abs(dist);
                target = z;
            }
        }
        return target;
    }

    private boolean applyMeleeToZombie(String name, Plant context, Zombie target, int damage) {
        target.takeDamage(damage);

        int r = rand.nextInt(5) + 1;
        context.triggerAction(r == 1 ? "attack" : "attack" + r);

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

    private Object findSingleMeleeObstacle(int plantRow, int plantCol, double fRange, double bRange) {
        Object targetObstacle = null;
        double minObsDistance = Double.MAX_VALUE;
        int minCol = (int) Math.max(0, Math.floor(plantCol + bRange));
        int maxCol = (int) Math.min(GameSession.getInstance().getArena().getCols() - 1, Math.floor(plantCol + fRange));

        for (int c = minCol; c <= maxCol; c++) {
            Tile tile = GameSession.getInstance().getArena().getTile(plantRow, c);
            boolean hasObstacle = (tile instanceof GraveHolder gh && gh.getGraveStone() != null) ||
                (tile instanceof IceHolder ih && ih.hasIceBlock());

            if (hasObstacle && Math.abs(c - plantCol) < minObsDistance) {
                minObsDistance = Math.abs(c - plantCol);
                targetObstacle = tile;
            }
            for (Plant p : tile.getPlants()) {
                if (p.isFrozen() && Math.abs(c - plantCol) < minObsDistance) {
                    minObsDistance = Math.abs(c - plantCol);
                    targetObstacle = p;
                }
            }
        }

        for (PushableObstacle obs : GameSession.getInstance().getArena().getActiveObstacles()) {
            if (!obs.isDestroyed() && obs.getRow() == plantRow) {
                double dist = obs.getCol() - plantCol;
                if (dist >= bRange && dist <= fRange && Math.abs(dist) < minObsDistance) {
                    minObsDistance = Math.abs(dist);
                    targetObstacle = obs;
                }
            }
        }
        return targetObstacle;
    }

    private boolean applyMeleeToObstacle(String name, Plant context, Object obstacle, int plantRow, int damage) {
        if (obstacle instanceof GraveHolder gh) {
            gh.takeDamage(damage, plantRow, ((Tile) obstacle).getCol());
        } else if (obstacle instanceof IceHolder ih) {
            ih.takeIceDamage(damage);
        } else if (obstacle instanceof Plant p) {
            p.damageIceBlock(damage);
        } else if (obstacle instanceof PushableObstacle obs) {
            obs.takeDamage(damage);
        }

        int r = rand.nextInt(5) + 1;
        context.triggerAction(r == 1 ? "attack" : "attack" + r);
        notify("🥊 " + name + " landed a melee strike on an obstacle!");
        return true;
    }

    private boolean executeAoEMelee(String name, Plant context, int plantRow, int plantCol, int baseDamage) {
        int finalDamage = baseDamage;

        if (name.equals("Kiwibeast")) {
            int stage = context.getSize();
            finalDamage = (stage == 3) ? baseDamage * 3 : (stage == 2 ? 30 : baseDamage);
        } else if (attackCount % 4 == 3) {
            finalDamage = baseDamage * 3;
            notify("💥 Phat Beet landed a CRITICAL thump!");
        }

        boolean attacked = applyAoEDamageToZombies(context, plantCol, plantRow, finalDamage);
        if (applyAoEDamageToObstacles(plantRow, plantCol, finalDamage)) {
            attacked = true;
        }

        if (attacked) {
            context.triggerAction(name.equals("Kiwibeast") ? "attack_stage" + context.getSize() : "attack");
            notify("🔊 " + name + " slammed a 3x3 area, hitting targets!");
        }

        return attacked;
    }

    private boolean applyAoEDamageToZombies(Plant context, int plantCol, int plantRow, int finalDamage) {
        List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 1.5);
        boolean attacked = false;

        for (Zombie z : targets) {
            if (!z.isDead()) {
                z.takeDamage(finalDamage);
                if (z.isDead()) {
                    context.onZombieDeath(z);
                }
                attacked = true;
            }
        }
        return attacked;
    }

    private boolean applyAoEDamageToObstacles(int plantRow, int plantCol, int finalDamage) {
        boolean attacked = false;
        int minRow = Math.max(0, plantRow - 1);
        int maxRow = Math.min(GameSession.getInstance().getArena().getRows() - 1, plantRow + 1);
        int minCol = Math.max(0, plantCol - 1);
        int maxCol = Math.min(GameSession.getInstance().getArena().getCols() - 1, plantCol + 1);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                Tile tile = GameSession.getInstance().getArena().getTile(r, c);

                if (tile instanceof GraveHolder gh && gh.getGraveStone() != null) {
                    gh.takeDamage(finalDamage, r, c);
                    attacked = true;
                }
                if (tile instanceof IceHolder ih && ih.hasIceBlock()) {
                    ih.takeIceDamage(finalDamage);
                    attacked = true;
                }
                for (Plant p : tile.getPlants()) {
                    if (p.isFrozen()) {
                        p.damageIceBlock(finalDamage);
                        attacked = true;
                    }
                }
            }
        }

        for (PushableObstacle obs : GameSession.getInstance().getArena().getActiveObstacles()) {
            if (!obs.isDestroyed() && obs.getRow() >= minRow && obs.getRow() <= maxRow
                && obs.getCol() >= minCol && obs.getCol() <= maxCol) {
                obs.takeDamage(finalDamage);
                attacked = true;
            }
        }
        return attacked;
    }

    public void increaseRange(float range) {
        this.rangeExtension += range;
    }
}
