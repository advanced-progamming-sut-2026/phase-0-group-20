package models.entities.zombies.behavior.attack;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieState;
import models.fields.tiles.Tile;
import models.timeManager.TimeManager;

public class SmashAttack implements AttackBehavior {
    private final Zombie zombie;
    private final int smashDamage;
    private int cooldownTicks;

    public SmashAttack(Zombie zombie, int smashDamage) {
        this.zombie = zombie;
        this.smashDamage = smashDamage;
        this.cooldownTicks = 0;
    }

    @Override
    public void execute() {
        if (cooldownTicks > 0) {
            cooldownTicks--;
            return;
        }

        Tile currentTile = zombie.getTile();

        if (currentTile == null || currentTile.getPlants().isEmpty()) {
            zombie.setAttacking(false);
            zombie.setState(ZombieState.WALKING);
            return;
        }

        Plant targetPlant = currentTile.getPlants().get(0);
        targetPlant.takeDamage(smashDamage);

        System.out.println(zombie.getName() + " smashed " + targetPlant.getName() + " for " + smashDamage + " damage!");

        if (targetPlant.getCurrentHp() <= 0) {
            currentTile.getPlants().remove(targetPlant);
            System.out.println(targetPlant.getName() + " was crushed!");
        }

        cooldownTicks = 10 * TimeManager.TICKS_PER_SECOND;
    }
}
