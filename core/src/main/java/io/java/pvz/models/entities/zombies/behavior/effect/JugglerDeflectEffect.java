package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;

public class JugglerDeflectEffect extends Effect {
    private final Plant targetPlant;
    private final ProjectileType type;
    private int timer;

    public JugglerDeflectEffect(Zombie zombie, Plant targetPlant, ProjectileType type) {
        super(zombie, -1);
        this.targetPlant = targetPlant;
        this.type = type;
        this.timer = (int) (0.5f * TimeManager.TICKS_PER_SECOND);
    }

    @Override
    public void onApply() { }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        timer--;

        if (timer <= 0) {
            if (targetPlant != null && !targetPlant.isDead()) {
                if (type == ProjectileType.ICE_PEA) {
                    targetPlant.receiveIceHit();
                } else {
                    targetPlant.takeDamage(20);
                }
            }
            zombie.getActiveEffects().remove(this);
        }
    }

    @Override
    public float getRemainingSeconds() { return 0f; }

    @Override
    public void onRemove() { }

    @Override
    public boolean isFinished() {
        return timer <= 0 || zombie.isDead();
    }
}
