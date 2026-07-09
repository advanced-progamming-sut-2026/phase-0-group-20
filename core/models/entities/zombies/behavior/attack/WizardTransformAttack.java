package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.plants.effect.CatEffect;
import models.entities.plants.effect.PlantEffect;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.fields.tiles.Tile;

public class WizardTransformAttack implements AttackBehavior {
    private final Zombie zombie;

    public WizardTransformAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        Tile currentTile = zombie.getTile();

        if (currentTile != null && !currentTile.getPlants().isEmpty()) {
            Plant targetPlant = null;
            for (Plant p : currentTile.getPlants()) {
                if (!isAlreadyCat(p)) {
                    targetPlant = p;
                    break;
                }
            }

            if (targetPlant != null) {
                targetPlant.addEffect(new CatEffect(zombie));
            }
        }

        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }

    private boolean isAlreadyCat(Plant plant) {
        if (plant.getActiveEffects() != null) {
            for (PlantEffect effect : plant.getActiveEffects()) {
                if (effect instanceof CatEffect) {
                    return true;
                }
            }
        }
        return false;
    }
}
