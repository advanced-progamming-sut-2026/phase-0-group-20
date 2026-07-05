package models.entities.plants.strategy.tag_strategy;

import models.entities.plants.Plant;
import models.entities.plants.strategy.IPlantStrategy;
import models.entities.zombies.Zombie;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;
import java.util.Random;

public class MoveZombiesStrategy implements IPlantStrategy {
    private final Random random = new Random();
    private int lastRecordedHp = -1;
    private int lastPullTick = 0;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        String name = context.getName();
        int plantRow = context.getPlacedTile().getRow();
        double plantCol = context.getPlacedTile().getCol();

        if (name.equals("Garlic")) {
            if (lastRecordedHp == -1) {
                lastRecordedHp = context.getCurrentHp();
                return;
            }

            int currentHp = context.getCurrentHp();

            if (currentHp < lastRecordedHp) {
                List<Zombie> attackers = gameSession.getArena().getZombiesInRadius(plantCol, plantRow, 0.8);

                for (Zombie z : attackers) {
                    if (!z.isDead() && z.getRow() == plantRow) {
                        moveZombieToAdjacentLane(z, plantRow, gameSession);
                        System.out.println("🧄 Garlic forced " + z.getName() + " to switch lanes!");
                    }
                }
                lastRecordedHp = currentHp;
            } else if (currentHp > lastRecordedHp) {
                lastRecordedHp = currentHp;
            }
        } else if (name.equals("Sweet Potato")) {
            int pullInterval = (int) (0.5 * TimeManager.TICKS_PER_SECOND);

            if (currentTick - lastPullTick >= pullInterval) {
                for (Zombie z : gameSession.getArena().getActiveZombies()) {
                    if (z.isDead()) continue;

                    int zRow = z.getRow();
                    if (Math.abs(zRow - plantRow) == 1) {
                        if (Math.abs(z.getX() - plantCol) <= 2.0) {
                            z.setRow(plantRow);
                            System.out.println("🍠 Sweet Potato pulled " + z.getName() + " into its lane!");
                        }
                    }
                }
                lastPullTick = currentTick;
            }
        }
    }

    private void moveZombieToAdjacentLane(Zombie zombie, int currentRow, GameSession gameSession) {
        int maxRows = gameSession.getArena().getRows();
        boolean canMoveUp = currentRow > 0;
        boolean canMoveDown = currentRow < (maxRows - 1);

        int targetRow = currentRow;

        if (canMoveUp && canMoveDown) {
            targetRow = random.nextBoolean() ? currentRow - 1 : currentRow + 1;
        } else if (canMoveUp) {
            targetRow = currentRow - 1;
        } else if (canMoveDown) {
            targetRow = currentRow + 1;
        }

        if (targetRow != currentRow) {
            zombie.setRow(targetRow);
        }
    }
}
