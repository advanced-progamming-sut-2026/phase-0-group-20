package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.fields.tiles.Tile;

public class TorchBurnAttack implements AttackBehavior {
    private final Zombie zombie;
    private boolean isTorchLit;

    public TorchBurnAttack(Zombie zombie) {
        this.zombie = zombie;
        this.isTorchLit = true;
    }

    @Override
    public void execute() {
        Tile currentTile = zombie.getTile();

        if (currentTile == null || currentTile.getPlants().isEmpty()) {
            resumeWalking();
            return;
        }

        Plant targetPlant = currentTile.getPlants().get(0);

        if (!isTorchLit) {
            int damagePerTick = zombie.getEatDps() / 10;
            targetPlant.takeDamage(damagePerTick);

            if (targetPlant.getCurrentHp() <= 0) {
                currentTile.getPlants().remove(targetPlant);
                if (currentTile.getPlants().isEmpty()) {
                    resumeWalking();
                }
            }
            return;
        }

        targetPlant.takeDamage(99999);
        currentTile.getPlants().remove(targetPlant);
        System.out.println(zombie.getName() + " instantly burned " + targetPlant.getName() + " to ashes!");

        resumeWalking();
    }

    private void resumeWalking() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.WALKING);
    }

    public void extinguishTorch() {
        if (this.isTorchLit) {
            this.isTorchLit = false;
            System.out.println(zombie.getName() + "'s torch was extinguished by ice!");
        }
    }

    public void igniteTorch() {
        if (!this.isTorchLit) {
            this.isTorchLit = true;
            System.out.println(zombie.getName() + "'s torch was reignited by fire!");
        }
    }

    public boolean isTorchLit() {
        return isTorchLit;
    }
}
