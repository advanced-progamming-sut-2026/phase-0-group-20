package com.Project.PVZ.models.entities.zombies.behavior.attack;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.plants.effect.CatEffect;
import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.entities.zombies.ZombieState;
import com.Project.PVZ.models.fields.tiles.Tile;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.timeManager.TimeManager;

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
