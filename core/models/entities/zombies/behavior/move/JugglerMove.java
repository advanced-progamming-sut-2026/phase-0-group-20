package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.context.JugglerContext;
import models.game.GameSession;

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
