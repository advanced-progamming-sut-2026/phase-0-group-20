package models.entities.zombies.behavior.effect;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.defense.DragonImpDefense;

public class FireEffect extends Effect {

    private int damage;
    private boolean isFinished;

    public FireEffect(Zombie zombie, int durationTicks, int damage) {
        super(zombie, durationTicks);
        this.damage = damage;
        isFinished = false;
    }


    @Override
    public void onApply() {
        zombie.takeDamage(damage);
        isFinished = true;
    }

    @Override
    public void execute() {
        onApply();

        if (isFinished) {
            onRemove();
        }
    }

    @Override
    public void onRemove() {
        zombie.getActiveEffects().remove(this);
    }
}
