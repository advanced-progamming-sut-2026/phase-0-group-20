package models.entities.plants.strategy;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.GameSession;

import java.util.List;

public class HypnotizeStrategy implements IPlantStrategy {
    private float healthMultiplier = 1.0f;
    private float damageMultiplier = 1.0f;


    @Override
    public void execute(Plant context, int currentTick) {
        int plantRow = context.getPlacedTile().getRow();
        int plantCol = context.getPlacedTile().getCol();

        List<Zombie> zombies = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 0.6);

        for (Zombie z : zombies) {
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
                break;
            }
        }
    }

    public void setHealthMultiplier(float multiplier) {
        this.healthMultiplier = multiplier;
    }

    public void setDamageMultiplier(float multiplier) {
        this.damageMultiplier = multiplier;
    }
}
