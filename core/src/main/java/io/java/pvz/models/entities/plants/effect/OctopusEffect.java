package io.java.pvz.models.entities.plants.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

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

            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("OCTOPUS_DIE")
                    .plant(plant)
                    .build());
        }
    }

    public int getHp() {
        return hp;
    }

    @Override
    public void apply(Plant plant) {
        plant.setStunned(true);
        notify(plant.getName() + " is covered by an OCTOPUS!");

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("OCTOPUS_LAND")
                .plant(plant)
                .build());
    }

    @Override
    public void execute(Plant plant, int currentTick) {

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
