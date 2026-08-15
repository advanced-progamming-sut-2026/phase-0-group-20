package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.timeManager.TimeManager;

public class SlideCooldownEffect extends Effect {
    public SlideCooldownEffect(Zombie zombie) {
        super(zombie, (int)(1.5f * TimeManager.TICKS_PER_SECOND));
    }

    @Override
    public void onApply() {}

    @Override
    public void onRemove() {
        zombie.getActiveEffects().remove(this);
    }
}
