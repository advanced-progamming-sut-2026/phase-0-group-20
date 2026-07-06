package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.fields.tiles.Tile;

import java.util.ArrayList;
import java.util.List;

public class SquashHit implements AttackBehavior {
    private final Zombie zombie;

    public SquashHit(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        Tile currentTile = zombie.getTile();

        if (currentTile == null || currentTile.getPlants().isEmpty()) {
            resumeWalking();
            return;
        }

        List<Plant> plantsToCrush = new ArrayList<>(currentTile.getPlants());

        for (Plant targetPlant : plantsToCrush) {
            targetPlant.takeDamage(99999);
            currentTile.getPlants().remove(targetPlant);
            System.out.println(zombie.getName() + " completely crushed " + targetPlant.getName() + "!");
        }

        resumeWalking();
    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
