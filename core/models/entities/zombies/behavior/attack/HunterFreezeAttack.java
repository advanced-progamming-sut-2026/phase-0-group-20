package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.plants.effect.FreezeEffect;
import models.entities.plants.effect.PlantEffect;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.game.GameSession;

public class HunterFreezeAttack implements AttackBehavior {
    private final Zombie zombie;

    public HunterFreezeAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        int row = zombie.getRow();

        Plant target = session.getArena().nearestPlantInRow(zombie, row);

        if (target != null) {
            FreezeEffect existingIce = null;

            for (PlantEffect effect : target.getActiveEffects()) {
                if (effect instanceof FreezeEffect) {
                    existingIce = (FreezeEffect) effect;
                    break;
                }
            }

            if (existingIce != null) {
                existingIce.addStack(target);
            } else {
                target.addEffect(new FreezeEffect());
            }
        }

        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
