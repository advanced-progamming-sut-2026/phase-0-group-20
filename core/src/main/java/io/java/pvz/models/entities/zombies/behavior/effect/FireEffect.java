package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.zombies.Zombie;

public class FireEffect extends Effect {

    private final int damage;

    public FireEffect(Zombie zombie, int damage) {
        super(zombie, 1);
        this.damage = damage;
    }

    @Override
    public void onApply() {
        zombie.setBurnedToAsh(true);
        zombie.takeDamage(damage);
    }

    @Override
    public void onRemove() {
        zombie.getActiveEffects().remove(this);
    }
}
