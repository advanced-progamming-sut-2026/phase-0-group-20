package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.behavior.context.AllStarContext;

public class AllStarMove implements MoveBehavior {
    private final Zombie zombie;
    private final AllStarContext context;
    private boolean speedIncreased = false;

    public AllStarMove(Zombie zombie, AllStarContext context) {
        this.zombie = zombie;
        this.context = context;
    }

    @Override
    public void execute() {
        if (zombie.getState() == ZombieState.SPECIAL) return;

        if (!context.hasTackled()) {
            if (!speedIncreased) {
                zombie.applySpeedMultiplier(3.0f);
                speedIncreased = true;
            }
            zombie.setState(ZombieState.WALKING);
            zombie.moveForward();
        } else {
            if (speedIncreased) {
                zombie.resetSpeed();
                speedIncreased = false;
            }
            zombie.setState(ZombieState.WALKING);
            zombie.moveForward();
        }
    }
}
