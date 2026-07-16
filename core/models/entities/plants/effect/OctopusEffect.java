package models.entities.plants.effect;

import models.entities.plants.Plant;

public class OctopusEffect implements PlantEffect {
    private int hp;

    public OctopusEffect(int hp) {
        this.hp = hp;
    }

    public void takeDamage(int damage) {
        this.hp -= damage;
    }

    public int getHp() {
        return hp;
    }


    @Override
    public void apply(Plant plant) {
        plant.setStunned(true);
        notify(plant.getName() + " is trapped under an Octopus!");
    }

    @Override
    public void execute(Plant plant, int currentTick) {
        // it must be empty
    }

    @Override
    public void remove(Plant plant) {
        plant.setStunned(false);
        notify("The Octopus on " + plant.getName() + " was destroyed!");
    }

    @Override
    public boolean isExpired() {
        return hp <= 0;
    }
}
