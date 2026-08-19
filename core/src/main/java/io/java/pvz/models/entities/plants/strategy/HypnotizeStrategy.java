package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;

public class HypnotizeStrategy implements IPlantStrategy {
    private float healthMultiplier = 1.0f;
    private float damageMultiplier = 1.0f;

    @Override
    public void execute(Plant context, int currentTick) {
    }

    public void onEatenBy(Plant context, Zombie z) {
        if (!z.isDead() && !z.isHypnotized()) {
            z.hypnotize();

            if (healthMultiplier > 1.0f) {
                z.setBaseHp((int) (z.getBaseHp() * healthMultiplier));
                z.setHealth((int) (z.getHealth() * healthMultiplier));
            }

            if (damageMultiplier > 1.0f) {
                z.setEatDPS((int) (z.getEatDPS() * damageMultiplier));
            }

            notify("😵 " + context.getName() + " hypnotized " + z.getName() + "!");

            context.takeDamage(context.getCurrentHp());
        }
    }

    public void setHealthMultiplier(float multiplier) {
        this.healthMultiplier = multiplier;
    }

    public void setDamageMultiplier(float multiplier) {
        this.damageMultiplier = multiplier;
    }
}
