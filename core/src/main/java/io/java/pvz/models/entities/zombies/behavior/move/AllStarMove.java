package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.context.AllStarContext;
import io.java.pvz.models.game.GameSession;

public class AllStarMove implements MoveBehavior {
    private final Zombie zombie;
    private final AllStarContext context;

    public AllStarMove(Zombie zombie, AllStarContext context) {
        this.zombie = zombie;
        this.context = context;
    }


    @Override
    public void execute() {
        if (!context.hasTackled()) {
            zombie.moveForward();
            zombie.moveForward();
            zombie.moveForward();
        } else {
            if (GameSession.getInstance().getTimeManager().getCurrentTick() % 2 == 0) {
                zombie.moveForward();
            }
        }
    }
}
