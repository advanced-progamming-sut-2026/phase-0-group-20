package models.entities.plants.strategy.category_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.zombies.Zombie;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;

import static models.entities.projectiles.ProjectileMechanism.executeNewProjectile;

public class ShootingStrategy implements IPlantStrategy {
    private int lastShotTick = 0;
    private int rangeExtension = 0;
    private float chillDurationExtension = 0;
    private int poisonTickDamageBonus = 0;

    private float autoPlantFoodChance = 0.0f;

    @Override
    public void execute(Plant context, int currentTick) {
        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (currentTick - lastShotTick) >= intervalInTicks) {
            boolean shootForward = false;
            boolean shootBackward = false;

            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            String plantName = context.getName();

            if (plantName.equals("Rotobaga")) {
                for (Zombie z : GameSession.getInstance().getChosenZombies()) {
                    if (z.isDead()) continue;
                    int rowDiff = Math.abs(z.getRow() - plantRow);
                    int colDiff = Math.abs(z.getCol() - plantCol);
                    if (rowDiff == colDiff && rowDiff > 0 && rowDiff <= 2) {
                        shootForward = true;
                        shootBackward = true;
                        break;
                    }
                }
            } else if (plantName.equals("Starfruit")) {
                for (Zombie z : GameSession.getInstance().getChosenZombies()) {
                    if (z.isDead()) continue;
                    int rowDiff = z.getRow() - plantRow;
                    int colDiff = z.getCol() - plantCol;

                    boolean isBackward = (rowDiff == 0 && colDiff < 0);
                    boolean isUpOrDown = (colDiff == 0 && rowDiff != 0);
                    boolean isDiagonalForward = (colDiff > 0 && Math.abs(rowDiff) == colDiff);

                    if (isBackward || isUpOrDown || isDiagonalForward) {
                        shootForward = true;
                        shootBackward = true;
                        break;
                    }
                }
            } else {
                List<Integer> targetLines = projectileInLine(plantName, plantRow);
                for (int line : targetLines) {
                    for (Zombie z : GameSession.getInstance().getArena().zombieInRow(line)) {
                        if (z.isDead()) continue;

                        int maxRange = (plantName.equals("Sea-shroom") || plantName.equals("Puff-shroom"))
                                ? (3 + rangeExtension) : 999;

                        if (z.getCol() >= plantCol && z.getCol() <= plantCol + maxRange) shootForward = true;

                        if (z.getCol() < plantCol) shootBackward = true;
                    }
                }
            }

            if (shootForward || shootBackward) {
                if (autoPlantFoodChance > 0 && Math.random() < autoPlantFoodChance) {
                    context.useFood();
                } else {
                    executeNewProjectile(context, shootForward, shootBackward);
                    notify(plantName + " fired projectiles!");
                }
                lastShotTick = currentTick;
            }
        }
    }

    private static List<Integer> projectileInLine(String name, int placedRow) {
        List<Integer> lines = new ArrayList<>();
        lines.add(placedRow);
        if (name.equals("Threepeater")) {
            lines.add(placedRow - 1);
            lines.add(placedRow + 1);
        }
        return lines;
    }

    private int getBurstCount(Plant context) {
        switch (context.getName()) {
            case "Rotobaga":
                return 3;
            case "Repeater":
                return 2;
            case "Pea Pod":
                return context.getStackCount();
            default:
                return 1;
        }
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