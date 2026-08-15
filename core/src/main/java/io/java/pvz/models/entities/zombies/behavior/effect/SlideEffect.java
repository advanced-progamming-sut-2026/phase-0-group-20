package io.java.pvz.models.entities.zombies.behavior.effect;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.timeManager.TimeManager;

public class SlideEffect extends Effect {
    private final float startY;
    private final float targetY;
    private final int targetRow;

    public SlideEffect(Zombie zombie, int targetRow) {
        super(zombie, (int)(0.5f * TimeManager.TICKS_PER_SECOND));
        this.targetRow = targetRow;
        this.startY = zombie.getPosition().getY();
        this.targetY = (targetRow * PhysicalConstants.TILE_HEIGHT) +
            (PhysicalConstants.TILE_HEIGHT / 2f) + PhysicalConstants.GRID_START_Y;
    }

    @Override
    public void onApply() {
        zombie.setAttacking(false);
        zombie.applySpeedMultiplier(0f);
    }

    @Override
    public void execute() {
        super.execute();
        if (isFinished()) return;

        float progress = (float) currentTick / durationTicks;
        float currentY = startY + (targetY - startY) * progress;

        zombie.getPosition().setY(currentY);
    }

    @Override
    public void onRemove() {
        zombie.setRow(targetRow);
        zombie.resetSpeed();
        zombie.getActiveEffects().remove(this);

        zombie.addEffect(new SlideCooldownEffect(zombie));
    }
}
