package models.entities.plants.effect;

import models.entities.plants.Plant;

public class FreezeEffect implements PlantEffect {
    private int stacks;
    private int hp;

    public FreezeEffect() {
        this.stacks = 1;
        this.hp = 800;
    }

    public void addStack(Plant plant) {
        if (stacks < 3) {
            stacks++;
            System.out.println(plant.getName() + " got chilled! Ice stacks: " + stacks);

            if (stacks >= 3) {
                plant.setFrozen(true);
                System.out.println(plant.getName() + " is now completely FROZEN!");
            }
        }
    }

    public void takeDamage(int damage) {
        if (isFullyFrozen()) {
            this.hp -= damage;
        }
    }

    public boolean isFullyFrozen() {
        return stacks >= 3;
    }

    public int getHp() {
        return hp;
    }


    @Override
    public void apply(Plant plant) {
        System.out.println(plant.getName() + " got chilled! Ice stacks: " + stacks);
    }

    @Override
    public void execute(Plant plant, int currentTick) {
        // it must be empty
    }

    @Override
    public void remove(Plant plant) {
        plant.setFrozen(false);
        System.out.println("The ice block on " + plant.getName() + " shattered!");
    }

    @Override
    public boolean isExpired() {
        return isFullyFrozen() && hp <= 0;
    }
}
