package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.context.ProspectorContext;
import models.enums.PhysicalConstants;

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

        if (context.shouldExplode()) {
            notify("Prospector Dynamite Exploded! Jumping to the back!");
            context.triggerJump();

            zombie.setCol(0);

            zombie.setX(PhysicalConstants.TILE_UNIT_LENGTH / 2f);
            return;
        }

        if (context.isMovingReverse()) {
            zombie.moveBackward();
        } else {
            zombie.moveForward();
        }
    }
}
