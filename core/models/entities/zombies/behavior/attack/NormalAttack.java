package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.plants.effect.CatEffect;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.fields.tiles.Tile;
import models.timeManager.TimeManager;

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

        int damagePerTick = zombie.getEatDps() / TimeManager.TICKS_PER_SECOND;
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

        if (targetPlant.getCurrentHp() <= 0) {
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
