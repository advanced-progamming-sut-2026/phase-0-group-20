package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieState;
import io.java.pvz.models.entities.zombies.behavior.context.JugglerContext;
import io.java.pvz.models.game.GameSession;

public class JugglerMove implements MoveBehavior {
    private final Zombie zombie;
    private final JugglerContext context;

    public JugglerMove(Zombie zombie, JugglerContext context) {
        this.zombie = zombie;
        this.context = context;
    }

    @Override
    public void execute() {
        context.tickTimer();
        syncAnimationState();

        if (context.isSpinning()) {
            zombie.moveForward();
            zombie.moveForward();
        } else {
            if (GameSession.getInstance().getTimeManager().getCurrentTick() % 2 == 0) {
                zombie.moveForward();
            }
        }
    }

    private void syncAnimationState() {
        switch (context.getPhase()) {
            case SPIN_UP   -> zombie.setState(ZombieState.SPIN_UP);
            case SPINNING  -> zombie.setState(ZombieState.SPINNING);
            case SPIN_DOWN -> zombie.setState(ZombieState.SPIN_DOWN);
            default -> {
                if (zombie.getState() == ZombieState.SPIN_UP
                    || zombie.getState() == ZombieState.SPINNING
                    || zombie.getState() == ZombieState.SPIN_DOWN) {
                    zombie.setState(ZombieState.WALKING);
                }
            }
        }
    }
}
