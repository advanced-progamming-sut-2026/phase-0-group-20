package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.effect.SheepEffect;
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
        if (currentTile == null) {
            resumeWalking();
            return;
        }

        int damagePerTick = zombie.getEatDps() / TimeManager.TICKS_PER_SECOND;

        List<Zombie> zombiesToEat = GameSession.getInstance().getArena().getZombiesOnTile(currentTile);
        Zombie targetZombie = null;
        for (Zombie tileZombie : zombiesToEat) {
            if (tileZombie.isHypnotized()) {
                targetZombie = tileZombie;
                break;
            }
        }

        if (targetZombie != null) {
            targetZombie.takeDamage(damagePerTick);
            if (!targetZombie.isDead()) {
                return;
            }
        }

        if (currentTile.getPlants().isEmpty()) {
            resumeWalking();
            return;
        }

        Plant targetPlant = currentTile.getPlants().getLast();
        for (int i = currentTile.getPlants().size() - 1; i >= 0; i--) {
            Plant p = currentTile.getPlants().get(i);
            boolean isCat = p.getActiveEffects().stream().anyMatch(e -> e instanceof SheepEffect);
            if (!isCat) {
                targetPlant = p;
                break;
            }
        }

        targetPlant.takeDamage(damagePerTick);
        if (targetPlant.isDead()) {
            currentTile.getPlants().remove(targetPlant);
            if (currentTile.getPlants().isEmpty()) {
                resumeWalking();
            }
        }
    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
