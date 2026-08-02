package io.java.pvz.models.entities.plants.effect;

import io.java.pvz.models.entities.plants.Plant;

public class FreezeEffect implements PlantEffect {
    private int stacks;
    private int hp;
    private boolean isBroken = false;

    public FreezeEffect() {
        this.stacks = 1;
        this.hp = 600;
    }

    public void addStack(Plant plant) {
        if (stacks < 3) {
            stacks++;
            notify(plant.getName() + " got hit by ice! Stacks: " + stacks);

            if (stacks >= 3) {
                plant.setFrozen(true);
                notify(plant.getName() + " is FROZEN!");
            }
        }
    }

    public void takeDamage(Plant plant, int damage) {
        if (!isFullyFrozen() || isBroken) return;

        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            this.isBroken = true;

            plant.setFrozen(false);
            System.out.println("Ice block broken! " + plant.getName() + " is free!");
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
        notify(plant.getName() + " got hit by ice! Stacks: " + stacks);
    }

    @Override
    public void execute(Plant plant, int currentTick) {
        // it must be empty
    }

    @Override
    public void remove(Plant plant) {
        plant.setFrozen(false);
    }

    @Override
    public boolean isExpired() {
        return isBroken;
    }
}
