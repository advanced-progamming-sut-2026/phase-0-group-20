package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.armour.Armor;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class MagnetStrategy implements IPlantStrategy {
    private int lastStealTick = -1;
    private float rangeExtension = 0;

    @Override
    public void execute(Plant context, int currentTick) {

        int intervalInTicks = (int) (context.getActionInterval() * TimeManager.TICKS_PER_SECOND);

        if (intervalInTicks > 0 && (lastStealTick == -1 || (currentTick - lastStealTick) >= intervalInTicks)){

            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            boolean foundMetal = false;

            float maxRowDist = 2.0f + rangeExtension;
            float maxColDist = 2.5f + rangeExtension;

            for (Zombie zombie : GameSession.getInstance().getArena().getActiveZombies()) {
                if (zombie.isDead()) continue;

                int rowDiff = Math.abs(zombie.getRow() - plantRow);
                double colDiff = Math.abs(zombie.getCol() - plantCol);

                if (rowDiff <= maxRowDist && colDiff <= maxColDist) {
                    for (Armor armor : zombie.getArmorPieces()) {

                        if (!armor.isDestroyed() && armor.isMetallic()) {
                            armor.takeDamage(9999);
                            foundMetal = true;

                            context.triggerAction("catch");

                            notify("🧲 " + context.getName() +
                                " magnetically stole a metallic armor from " + zombie.getName() + "!");
                            break;
                        }
                    }
                }
                if (foundMetal) break;
            }

            if (foundMetal) {
                lastStealTick = currentTick;
            }
        }
    }

    public void increaseRange(float extraRange) {
        this.rangeExtension += extraRange;
    }
}
