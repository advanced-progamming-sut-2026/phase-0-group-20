package com.Project.PVZ.models.entities.plants.strategy.category_strategy;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.plants.strategy.IPlantStrategy;
import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.enums.PhysicalConstants;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

import static com.Project.PVZ.models.entities.projectiles.ProjectileMechanism.executeNewProjectile;

public class ShootingStrategy implements IPlantStrategy {
    private int lastShotTick = 0;
    private int rangeExtension = 0;
    private float chillDurationExtension = 0;
    private int poisonTickDamageBonus = 0;
    private int pendingShots = 0;
    private int burstCooldownTicks = 0;
    private boolean currentShootForward = false;
    private boolean currentShootBackward = false;
    private float autoPlantFoodChance = 0.0f;

    private static List<Integer> projectileInLine(String name, int placedRow) {
        List<Integer> lines = new ArrayList<>();
        lines.add(placedRow);
        if (name.equals("Threepeater")) {
            lines.add(placedRow - 1);
            lines.add(placedRow + 1);
        }
        return lines;
    }

    @Override
    public void execute(Plant context, int currentTick) {
        if (pendingShots > 0) {
            if (burstCooldownTicks > 0) {
                burstCooldownTicks--;
            } else {
                executeNewProjectile(context, currentShootForward, currentShootBackward);
                pendingShots--;
                burstCooldownTicks = 2;
            }
            return;
        }

        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            boolean[] directions = determineShootDirections(context);

            if (directions[0] || directions[1]) {
                handleFiring(context, directions[0], directions[1], currentTick);
            }
        }
    }

    private boolean[] determineShootDirections(Plant context) {
        String plantName = context.getName();

        if (plantName.equals("Rotobaga") || plantName.equals("Starfruit")) {
            boolean canShoot = checkMultiDirectionalTargets(context, plantName);
            return new boolean[]{canShoot, canShoot};
        } else {
            return checkLinearTargets(context, plantName);
        }
    }

    private boolean checkMultiDirectionalTargets(Plant context, String plantName) {
        int plantRow = context.getPlacedTile().getRow();
        int plantCol = context.getPlacedTile().getCol();

        for (Zombie z : GameSession.getInstance().getArena().getActiveZombies()) {
            if (z.isDead()) continue;

            if (plantName.equals("Rotobaga")) {
                int rowDiff = Math.abs(z.getRow() - plantRow);
                int colDiff = Math.abs(z.getCol() - plantCol);

                if (rowDiff == colDiff && rowDiff > 0) return true;
            } else {
                int zRow = z.getRow();
                int zCol = (int) (z.getX() / PhysicalConstants.TILE_UNIT_LENGTH);

                int rowDiff = zRow - plantRow;
                int colDiff = zCol - plantCol;

                boolean isBackward = (rowDiff == 0 && colDiff < 0);
                boolean isUpOrDown = (colDiff == 0 && rowDiff != 0);
                boolean isDiagonalForward = (colDiff > 0 && Math.abs(rowDiff) == colDiff);

                if (isBackward || isUpOrDown || isDiagonalForward) return true;
            }
        }
        return false;
    }

    private boolean[] checkLinearTargets(Plant context, String plantName) {
        boolean shootForward = false;
        boolean shootBackward = false;
        int plantRow = context.getPlacedTile().getRow();
        int plantCol = context.getPlacedTile().getCol();

        List<Integer> targetLines = projectileInLine(plantName, plantRow);
        for (int line : targetLines) {
            if (line < 0 || line >= GameSession.getInstance().getArena().getRows()) continue;

            for (Zombie z : GameSession.getInstance().getArena().zombieInRow(line)) {
                if (z.isDead()) continue;

                int maxRange = (plantName.equals("Sea-shroom") || plantName.equals("Puff-shroom"))
                        ? (3 + rangeExtension) : 999;

                if (z.getCol() >= plantCol && z.getCol() <= plantCol + maxRange) shootForward = true;

                if (z.getCol() < plantCol) shootBackward = true;
            }
        }
        return new boolean[]{shootForward, shootBackward};
    }

    private void handleFiring(Plant context, boolean shootForward, boolean shootBackward, int currentTick) {
        if (autoPlantFoodChance > 0 && Math.random() < autoPlantFoodChance) {
            context.useFood();
        } else {
            int stacks = context.getStackCount();
            executeNewProjectile(context, shootForward, shootBackward);
            notify(context.getName() + " fired a projectile!");

            if (stacks > 1) {
                this.pendingShots = stacks - 1;
                this.burstCooldownTicks = 5;
                this.currentShootForward = shootForward;
                this.currentShootBackward = shootBackward;
            }
        }
        lastShotTick = currentTick;
    }
    public void increaseRange(int range) {
        this.rangeExtension += range;
    }

    public void increaseChillDuration(float duration) {
        this.chillDurationExtension += duration;
    }

    public float getChillDurationExtension() {
        return chillDurationExtension;
    }

    public void increasePoisonTickDamage(int amount) {
        this.poisonTickDamageBonus += amount;
    }

    public int getPoisonTickDamageBonus() {
        return poisonTickDamageBonus;
    }

    public void setAutoPlantFoodChance(float chance) {
        this.autoPlantFoodChance = chance;
    }
}
