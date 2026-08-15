package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;

public class SpikeStrategy implements IPlantStrategy {
    private static final int DAMAGE_INTERVAL = TimeManager.TICKS_PER_SECOND;
    private int lastDamageTick = -1;
    private int reflectDamageBonus = 0;
    private boolean hasArmor = false;


    @Override
    public void execute(Plant context, int currentTick) {
        if (lastDamageTick == -1) {
            lastDamageTick = currentTick;
            return;
        }

        if (currentTick - lastDamageTick >= DAMAGE_INTERVAL) {
            boolean dealtDamage = false;
            List<Zombie> attackingZombies =
                GameSession.getInstance().getArena().getZombiesOnTile(context.getPlacedTile());

            for (Zombie z : attackingZombies) {
                if (!z.isDead()) {
                    int currentDamage = context.getDamage();
                    if (currentDamage <= 0) currentDamage = 20;
                    currentDamage += reflectDamageBonus;

                    if (hasArmor) {
                        currentDamage *= 2;
                    }

                    z.takeDamage(currentDamage);
                    if (z.isDead()) {
                        context.onZombieDeath(z);
                    }
                    dealtDamage = true;
                }
            }

            if (dealtDamage) {
                if (context.getName().equals("Endurian")) {
                    context.triggerAction("attack_loop");
                } else {
                    context.triggerAction("attack");
                }

                notify("🦔 " + context.getName() + " reflected damage to attacking zombies!");
                lastDamageTick = currentTick;
            }
        }
    }

    public void setHasArmor(boolean hasArmor) {
        this.hasArmor = hasArmor;
    }

    public void increaseReflectDamage(int amount) {
        this.reflectDamageBonus += amount;
    }

}
