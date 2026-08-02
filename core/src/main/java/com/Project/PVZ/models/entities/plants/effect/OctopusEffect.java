package com.Project.PVZ.models.entities.plants.effect;

import com.Project.PVZ.models.entities.plants.Plant;

public class OctopusEffect implements PlantEffect {
    private int hp;
    private boolean isDestroyed = false;

    public OctopusEffect(int hp) {
        this.hp = hp;
    }

    public void takeDamage(Plant plant, int damage) {
        if (isDestroyed) return;

        this.hp -= damage;
        if (this.hp <= 0) {
            this.hp = 0;
            this.isDestroyed = true;

            plant.setStunned(false);
            notify("Octopus destroyed! " + plant.getName() + " is free!");
        }
    }

    public int getHp() {
        return hp;
    }

    @Override
    public void apply(Plant plant) {
        plant.setStunned(true);
        notify(plant.getName() + " is covered by an OCTOPUS!");
    }

    @Override
    public void execute(Plant plant, int currentTick) {
        // it must be empty
    }

    @Override
    public void remove(Plant plant) {
        plant.setStunned(false);
    }

    @Override
    public boolean isExpired() {
        return isDestroyed;
    }
}
