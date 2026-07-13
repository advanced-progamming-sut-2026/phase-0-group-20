package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.List;

/**
 * Spike Strategy (Adapted for Endurian):
 * Defensive plants with this strategy act as a wall but also deal continuous
 * physical damage to zombies that are close enough to attack/eat them.
 */

public class SpikeStrategy implements IPlantStrategy {
    private final int DAMAGE_INTERVAL = TimeManager.TICKS_PER_SECOND;
    private int lastDamageTick = 0;

    @Override
    public void execute(Plant context, int currentTick, GameSession gameSession) {
        if (currentTick - lastDamageTick >= DAMAGE_INTERVAL) {
            int plantRow = context.getPlacedTile().getRow();
            double plantCol = context.getPlacedTile().getCol();
            boolean dealtDamage = false;

            List<Zombie> attackingZombies = gameSession.getArena().getZombiesInRadius((int) plantCol, plantRow, 0.8);

            for (Zombie z : attackingZombies) {
                if (!z.isDead()) {
                    int damage = 20;
                    try {
                        damage = Integer.parseInt(context.getDamage());
                    } catch (NumberFormatException e) {
                        damage = 20;
                    }

                    z.takeDamage(damage);
                    dealtDamage = true;
                }
            }

            if (dealtDamage) {
                System.out.println("🦔 " + context.getName() + " reflected " + context.getDamage() + " damage to attacking zombies!");
                lastDamageTick = currentTick;
            }
        }
    }
}
