package com.Project.PVZ.models.entities.zombies.behavior.attack;

import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.plants.effect.CatEffect;
import com.Project.PVZ.models.entities.plants.effect.PlantEffect;
import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.entities.zombies.ZombieState;
import com.Project.PVZ.models.fields.tiles.Tile;
import com.Project.PVZ.models.game.GameSession;

public class WizardTransformAttack implements AttackBehavior {
    private final Zombie zombie;

    public WizardTransformAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        Tile currentTile = GameSession.getInstance().getArena().getTile(zombie.getRow(), zombie.getCol());

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
