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
    boolean hasArmor = false;
    int damage;


    @Override
    public void execute(Plant context, int currentTick) {
        if (currentTick - lastDamageTick >= DAMAGE_INTERVAL) {
            int plantRow = context.getPlacedTile().getRow();
            int plantCol = context.getPlacedTile().getCol();
            boolean dealtDamage = false;


            List<Zombie> attackingZombies = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 0.8);

            for (Zombie z : attackingZombies) {
                damage = 20;
                if (!z.isDead()) {
                    try {
                        damage = Integer.parseInt(context.getDamage());
                    } catch (NumberFormatException e) {
                    }

                    hasArmor = context.getCurrentHp() > context.getBaseHp();

                    if (hasArmor) //more hp = having armor
                        damage *= 2;

                    z.takeDamage(damage);
                    dealtDamage = true;
                }
            }

            if (dealtDamage) {
                int d = hasArmor ? damage * 2 : 20;
                notify("🦔 " + context.getName() + " reflected " + d + " damage to attacking zombies!");
                lastDamageTick = currentTick;
            }
        }
    }

    public void setHasArmor(boolean hasArmor) {
        this.hasArmor = hasArmor;
    }

}
