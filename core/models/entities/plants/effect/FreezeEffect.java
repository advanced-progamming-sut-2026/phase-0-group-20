package models.entities.plants.effect;

import models.entities.plants.Plant;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

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
            notify(plant.getName() + " got chilled! Ice stacks: " + stacks);

            if (stacks >= 3) {
                plant.setFrozen(true);
                notify(plant.getName() + " is now completely FROZEN!");
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
        notify(plant.getName() + " got chilled! Ice stacks: " + stacks);
    }

    @Override
    public void execute(Plant plant, int currentTick) {
        // it must be empty
    }

    @Override
    public void remove(Plant plant) {
        plant.setFrozen(false);
        notify("The ice block on " + plant.getName() + " shattered!");
    }

    @Override
    public boolean isExpired() {
        return isFullyFrozen() && hp <= 0;
    }
}
