package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.context.ProspectorContext;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;

public class ProspectorMove implements MoveBehavior {
    private final Zombie zombie;
    private final ProspectorContext context;

    public ProspectorMove(Zombie zombie, ProspectorContext context) {
        this.zombie = zombie;
        this.context = context;
    }

    @Override
    public void execute() {
        context.tickTimer();

        if (context.shouldExplode() && zombie.getCol() < GameSession.getInstance().getArena().getCols()) {
            notify("Prospector Dynamite Exploded! Jumping to the back!");
            context.triggerJump();

            zombie.setCol(0);

            zombie.setX(PhysicalConstants.GRID_START_X + PhysicalConstants.TILE_WIDTH / 2);
            return;
        }

        if (context.isMovingReverse()) {
            float currentSpeed = zombie.getCurrentSpeed();
            if (currentSpeed > 0)
                zombie.setCurrentSpeed(-currentSpeed);
        }

        zombie.move();

    }
}
