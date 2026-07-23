package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.fields.tiles.Tile;
import models.game.GameSession;

public class SmashAttack implements AttackBehavior {
    private final Zombie zombie;
    private final int smashDamage;

    public SmashAttack(Zombie zombie, int smashDamage) {
        this.zombie = zombie;
        this.smashDamage = smashDamage;
    }

    @Override
    public void execute() {
        Tile currentTile = GameSession.getInstance().getArena().getTile(zombie.getRow(), zombie.getCol());

        if (currentTile == null || currentTile.getPlants().isEmpty()) {
            resumeWalking();
            return;
        }

        Plant targetPlant = currentTile.getPlants().get(0);

        int lethalDamage = Math.max(smashDamage, targetPlant.getCurrentHp());
        targetPlant.takeDamage(lethalDamage);
        currentTile.getPlants().remove(targetPlant);

        notify("Gargantuar Smashed " + targetPlant.getName() + " with his weapon!");

        resumeWalking();
    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }
}
