package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.entities.zombies.behavior.move.AllStarMove;
import models.entities.zombies.behavior.move.MoveBehavior;
import models.fields.tiles.Tile;

public class AllStarSmashAttack implements AttackBehavior {
    private final Zombie zombie;

    public AllStarSmashAttack(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        Tile currentTile = zombie.getTile();

        if (currentTile == null || currentTile.getPlants().isEmpty()) {
            resumeWalking();
            return;
        }

        Plant targetPlant = currentTile.getPlants().get(0);
        int lethalDamage = Math.max(targetPlant.getCurrentHp(), 1);
        targetPlant.takeDamage(lethalDamage);
        currentTile.getPlants().remove(targetPlant);

        System.out.println(zombie.getName() + " trampled " + targetPlant.getName() + " instantly!");

        MoveBehavior move = zombie.getMoveBehavior();
        if (move instanceof AllStarMove allStarMove) {
            allStarMove.stopRunning();
        }

        resumeWalking();
    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
