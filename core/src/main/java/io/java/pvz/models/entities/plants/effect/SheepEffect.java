package io.java.pvz.models.entities.plants.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public class SheepEffect implements PlantEffect {
    private final Zombie wizardZombie;

    public SheepEffect(Zombie wizardZombie) {
        this.wizardZombie = wizardZombie;
    }

    @Override
    public void apply(Plant plant) {
        plant.setStunned(true);

        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY)
                .message("SHEEP_APPLY")
                .plant(plant)
                .build());

        notify(plant.getName() + " was transformed into a SHEEP by " + wizardZombie.getName() + "!");
    }

    @Override
    public void execute(Plant plant, int currentTick) {
    }

    @Override
    public void remove(Plant plant) {
        plant.setStunned(false);

        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY)
                .message("SHEEP_REMOVE")
                .plant(plant)
                .build());

        notify("The spell broke! " + plant.getName() + " is back.");
    }

    @Override
    public boolean isExpired() {
        return wizardZombie.isDead();
    }
}
