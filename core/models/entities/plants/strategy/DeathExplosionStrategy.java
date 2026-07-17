package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.List;

public class DeathExplosionStrategy implements IPlantStrategy {
    private boolean hasExploded = false;

    private int extraExplosionDamage = 0;

    @Override
    public void execute(Plant context, int currentTick) {
        if (!hasExploded && context.getCurrentHp() <= 0) {
            hasExploded = true;

            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();

            int baseDamage = (int) context.getAbilityValue();
            if (baseDamage == 0) baseDamage = 1800;

            int finalDamage = baseDamage + extraExplosionDamage;

            notify("💥 " + context.getName() + " was eaten and DETONATED!");

            List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 1.5f);
            for (Zombie z : targets) {
                if (!z.isDead()) {
                    boolean killed = z.takeDirectDamage(finalDamage);
                    if (killed) {
                        context.onZombieDeath(z);
                    }
                }
            }
        }
    }

    public void increaseExplosionDamage(int amount) {
        this.extraExplosionDamage += amount;
    }
}
