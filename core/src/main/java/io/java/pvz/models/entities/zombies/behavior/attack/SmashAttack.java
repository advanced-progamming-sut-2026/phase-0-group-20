package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.behavior.effect.Effect;
import io.java.pvz.models.entities.zombies.behavior.effect.GargantuarSmashEffect;
import io.java.pvz.models.entities.zombies.behavior.effect.ZombieEffect;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;

public class SmashAttack implements AttackBehavior {
    private final Zombie zombie;
    private final int smashDamage;

    public SmashAttack(Zombie zombie, int smashDamage) {
        this.zombie = zombie;
        this.smashDamage = smashDamage;
    }

    @Override
    public void execute() {
        if (zombie.getActiveEffects() != null) {
            for (ZombieEffect effect : zombie.getActiveEffects()) {
                if (effect instanceof GargantuarSmashEffect) {
                    return;
                }
            }
        }

        Tile currentTile = GameSession.getInstance().getArena().getTile(zombie.getRow(), zombie.getCol());
        if (currentTile == null || currentTile.getPlants().isEmpty()) {
            resumeWalking();
            return;
        }

        Plant targetPlant = null;
        for (int i = currentTile.getPlants().size() - 1; i >= 0; i--) {
            Plant p = currentTile.getPlants().get(i);
            if (!p.isDead()) {
                targetPlant = p;
                break;
            }
        }

        if (targetPlant == null) {
            resumeWalking();
            return;
        }
        zombie.addEffect(new GargantuarSmashEffect(zombie, targetPlant, smashDamage, currentTile));
    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        if (zombie.getState() != ZombieState.THROW_IMP) {
            zombie.setState(ZombieState.WALKING);
        }
    }
}
