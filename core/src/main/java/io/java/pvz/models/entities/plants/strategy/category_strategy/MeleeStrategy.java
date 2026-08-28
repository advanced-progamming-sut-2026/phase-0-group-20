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

        int damage = name.equals("Wasabi Whip") ? Math.max(baseDamage, 40) : baseDamage;

        if (target != null) {
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

        Object targetObstacle = null;
        double minObsDistance = Double.MAX_VALUE;

        int minCol = (int) Math.max(0, Math.floor(plantCol + backwardRange));
        int maxCol = (int) Math.min(GameSession.getInstance().getArena().getCols() - 1, Math.floor(plantCol + forwardRange));

        for (int c = minCol; c <= maxCol; c++) {
            Tile tile = GameSession.getInstance().getArena().getTile(plantRow, c);
            boolean hasObstacle = false;

            if (tile instanceof GraveHolder gh && gh.getGraveStone() != null) {
                hasObstacle = true;
            } else if (tile instanceof IceHolder ih && ih.hasIceBlock()) {
                hasObstacle = true;
            }

            if (hasObstacle) {
                double dist = Math.abs(c - plantCol);
                if (dist < minObsDistance) {
                    minObsDistance = dist;
                    targetObstacle = tile;
                }
            }

            for (Plant p : tile.getPlants()) {
                if (p.isFrozen()) {
                    double dist = Math.abs(c - plantCol);
                    if (dist < minObsDistance) {
                        minObsDistance = dist;
                        targetObstacle = p;
                    }
                }
            }
        }

        for (PushableObstacle obs : GameSession.getInstance().getArena().getActiveObstacles()) {
            if (!obs.isDestroyed() && obs.getRow() == plantRow) {
                double dist = obs.getCol() - plantCol;
                if (dist >= backwardRange && dist <= forwardRange) {
                    if (Math.abs(dist) < minObsDistance) {
                        minObsDistance = Math.abs(dist);
                        targetObstacle = obs;
                    }
                }
            }
        }

        if (targetObstacle != null) {
            if (targetObstacle instanceof GraveHolder gh) {
                gh.takeDamage(damage, plantRow, ((Tile) targetObstacle).getCol());
            } else if (targetObstacle instanceof IceHolder ih) {
                ih.takeIceDamage(damage);
            } else if (targetObstacle instanceof Plant p) {
                p.damageIceBlock(damage);
            } else if (targetObstacle instanceof PushableObstacle obs) {
                obs.takeDamage(damage);
            }

            int r = rand.nextInt(5) + 1;
            String animName = r == 1 ? "attack" : "attack" + r;
            context.triggerAction(animName);

            notify("🥊 " + name + " landed a melee strike on an obstacle!");
            return true;
        }

        return false;
    }

    private boolean executeAoEMelee(String name, Plant context, int plantRow, int plantCol, int baseDamage) {
        List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 1.5);
        boolean attacked = false;

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

        if (!targets.isEmpty()) {
            for (Zombie z : targets) {
                if (!z.isDead()) {
                    z.takeDamage(finalDamage);
                    if (z.isDead()) {
                        context.onZombieDeath(z);
                    }
                    attacked = true;
                }
            }
        }

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
            if (!obs.isDestroyed() && obs.getRow() >= minRow && obs.getRow() <= maxRow && obs.getCol() >= minCol && obs.getCol() <= maxCol) {
                obs.takeDamage(finalDamage);
                attacked = true;
            }
        }

        if (attacked) {
            if (name.equals("Kiwibeast")) {
                context.triggerAction("attack_stage" + context.getSize());
            } else {
                context.triggerAction("attack");
            }
            notify("🔊 " + name + " slammed a 3x3 area, hitting targets!");
        }

        return attacked;
    }

    public void increaseRange(float range) {
        this.rangeExtension += range;
    }
}
