package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;

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
