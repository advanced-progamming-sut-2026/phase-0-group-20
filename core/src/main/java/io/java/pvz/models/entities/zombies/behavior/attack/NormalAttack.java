package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.effect.CatEffect;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.List;

public class NormalAttack implements AttackBehavior {
    private final Zombie zombie;

    public NormalAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        Tile currentTile = zombie.getTile();
        if (currentTile == null || currentTile.getPlants().isEmpty()) {
            resumeWalking();
            return;
        }
        boolean isZombieToEat = false;
        int damagePerTick = zombie.getEatDps() / TimeManager.TICKS_PER_SECOND;
        List<Zombie> zombiesToEat = GameSession.getInstance().getArena().getZombiesOnTile(currentTile);
        Zombie targetZombie = null;
        for (Zombie zombie : zombiesToEat) {
            if (zombie.isHypnotized()) {
                targetZombie = zombie;
                isZombieToEat = true;
                break;
            }
        }

        if (targetZombie != null) {
            targetZombie.takeDamage(damagePerTick);
            if (targetZombie.isDead()) {
                isZombieToEat = false;
            }
        }

        if (!isZombieToEat) {
            Plant targetPlant = currentTile.getPlants().get(0);
            for (Plant p : currentTile.getPlants()) {
                boolean isCat = p.getActiveEffects().stream().anyMatch(e -> e instanceof CatEffect);
                if (!isCat) {
                    targetPlant = p;
                    break;
                }
            }
            if (targetPlant == null) {
                resumeWalking();
                return;
            }
            targetPlant.takeDamage(damagePerTick);

            if (targetPlant.isDead()) {
                currentTile.getPlants().remove(targetPlant);

                if (currentTile.getPlants().isEmpty()) {
                    resumeWalking();
                }
            }
        }
    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
