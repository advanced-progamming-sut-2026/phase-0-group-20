package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.timeManager.TimeManager;

public class GargantuarSmashEffect extends Effect {
    private final Plant targetPlant;
    private final int smashDamage;
    private int ticksCounter = 0;
    private Tile currentTile;

    private final int totalTicks = (int) (1.7667f * TimeManager.TICKS_PER_SECOND);
    private final int smashTick = (int) (totalTicks * 0.5f);

    private boolean hasSmashed = false;
    private boolean isFinished = false;

    public GargantuarSmashEffect(Zombie zombie, Plant targetPlant, int smashDamage,  Tile currentTile) {
        super(zombie, -1);
        this.targetPlant = targetPlant;
        this.smashDamage = smashDamage;
        this.currentTile = currentTile;
    }

    @Override
    public void onApply() {
        zombie.setAttacking(false);
        zombie.setState(ZombieState.SMASH);
        zombie.applySpeedMultiplier(0f);
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished) return;

        zombie.setState(ZombieState.SMASH);
        zombie.setAttacking(false);

        ticksCounter++;

        if (ticksCounter == smashTick && !hasSmashed) {
            if (targetPlant != null && !targetPlant.isDead()) {
                targetPlant.takeDamage(99999);
                currentTile.removePlant(targetPlant);
                notify("Gargantuar Smashed " + targetPlant.getName() + " with his weapon!");
            }
            hasSmashed = true;
        }

        if (ticksCounter >= totalTicks) {
            isFinished = true;
            if (!zombie.isDead()) {
                zombie.setState(ZombieState.WALKING);
                zombie.resetSpeed();
            }
            zombie.getActiveEffects().remove(this);
        }
    }

    @Override
    public float getRemainingSeconds() { return 0f; }

    @Override
    public void onRemove() {
        if (!isFinished && !zombie.isDead()) {
            zombie.setState(ZombieState.WALKING);
            zombie.resetSpeed();
        }
        zombie.getActiveEffects().remove(this);
    }

    @Override
    public boolean isFinished() {
        return isFinished || zombie.isDead();
    }
}
