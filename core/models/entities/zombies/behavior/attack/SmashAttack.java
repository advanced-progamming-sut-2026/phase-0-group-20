package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.fields.tiles.Tile;

public class SmashAttack implements AttackBehavior {
    private final Zombie zombie;
    private final int smashDamage;

    public SmashAttack(Zombie zombie, int smashDamage) {
        this.zombie = zombie;
        this.smashDamage = smashDamage;
    }

    @Override
    public void execute() {
        Tile currentTile = zombie.getTile();

        if (currentTile == null || currentTile.getPlants().isEmpty()) {
            resumeWalking();
            return;
        }

        Plant targetPlant = currentTile.getPlants().get(0);

        int lethalDamage = Math.max(smashDamage, targetPlant.getCurrentHp());
        targetPlant.takeDamage(lethalDamage);
        currentTile.getPlants().remove(targetPlant);

        notify(zombie.getName() + " smashed " + targetPlant.getName() + " to bits!");

        resumeWalking();
    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
