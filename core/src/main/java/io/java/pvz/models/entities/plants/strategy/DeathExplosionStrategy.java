package io.java.pvz.models.entities.plants.strategy;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.FireEffect;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

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
            GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_EXPLODED,new GameEventPayload
                .Builder(GameEvent.PLANT_EXPLODED)
                .build());
            List<Zombie> targets = GameSession.getInstance().getArena().getZombiesInRadius(plantCol, plantRow, 1.5f);
            for (Zombie z : targets) {
                if (!z.isDead()) {
                    z.addEffect(new FireEffect(z, finalDamage));
                    if (z.isDead()) {
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
