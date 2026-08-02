package io.java.pvz.models.entities.plants.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;

public class CatEffect implements PlantEffect {
    private final Zombie wizardZombie;

    public CatEffect(Zombie wizardZombie) {
        this.wizardZombie = wizardZombie;
    }

    @Override
    public void apply(Plant plant) {
        // flag for this

        // plant.setTargetable(false);
        notify(plant.getName() + " was transformed into a CAT by " + wizardZombie.getName() + "!");

    }

    @Override
    public void execute(Plant plant, int currentTick) {

    }

    @Override
    public void remove(Plant plant) {
        notify("The spell broke! " + plant.getName() + " is no longer a cat.");
    }

    @Override
    public boolean isExpired() {
        return wizardZombie.isDead();
    }
}
