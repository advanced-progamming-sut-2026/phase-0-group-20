package com.Project.PVZ.models.entities.zombies.behavior.move;

import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.entities.zombies.behavior.context.JugglerContext;
import com.Project.PVZ.models.game.GameSession;

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

        if (context.isSpinning()) {
            zombie.moveForward();
            zombie.moveForward();
        } else {
            if (GameSession.getInstance().getTimeManager().getCurrentTick() % 2 == 0) {
                zombie.moveForward();
            }
        }
    }
}
