package io.java.pvz.models.entities.plants.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public class FreezeEffect implements PlantEffect {
    private int stacks;
    private int hp;
    private boolean isBroken = false;
    public static final int MAX_HP = 600;

    public FreezeEffect() {
        this.stacks = 1;
        this.hp = MAX_HP;
    }

    @Override
    public void apply(Plant plant) {
        sendOverlayEvent(plant);
    }

    public void addStack(Plant plant) {
        if (stacks < 3) {
            stacks++;

            if (stacks >= 3)
                plant.setFrozen(true);

            sendOverlayEvent(plant);
        }
    }

    private void sendOverlayEvent(Plant plant) {
        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("UPDATE_ICE_OVERLAY")
                .plant(plant)
                .amount(stacks)
                .build());
    }

    public void takeDamage(Plant plant, int damage) {
        if (!isFullyFrozen() || isBroken) return;

        this.hp -= damage;

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("ICE_BLOCK_DAMAGE")
                .coordinate(plant.getPlacedTile().getRow(), plant.getPlacedTile().getCol())
                .build());

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("UPDATE_ICE_CRACKS")
                .plant(plant)
                .amount(this.hp)
                .build());

        if (this.hp <= 0) {
            this.hp = 0;
            this.isBroken = true;

            plant.setFrozen(false);
            System.out.println("Ice block broken! " + plant.getName() + " is free!");

            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("REMOVE_ICE_OVERLAY")
                    .plant(plant)
                    .build());
        }
    }

    public boolean isFullyFrozen() { return stacks >= 3; }
    public int getHp() { return hp; }

    @Override
    public void execute(Plant plant, int currentTick) { }

    @Override
    public void remove(Plant plant) {
        plant.setFrozen(false);
    }

    @Override
    public boolean isExpired() {
        return isBroken;
    }
}
