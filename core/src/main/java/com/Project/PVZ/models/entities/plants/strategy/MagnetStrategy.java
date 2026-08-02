package com.Project.PVZ.models.entities.plants.strategy;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.entities.zombies.armour.Armor;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.timeManager.TimeManager;

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
