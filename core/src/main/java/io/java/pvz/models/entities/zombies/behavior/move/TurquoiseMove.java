package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.context.TurquoiseContext;
import io.java.pvz.models.game.GameSession;

public class TurquoiseMove implements MoveBehavior {
    private final Zombie zombie;
    private final TurquoiseContext context;
    private static final int STEAL_AMOUNT = 25;

    public TurquoiseMove(Zombie zombie, TurquoiseContext context) {
        this.zombie = zombie;
        this.context = context;
    }

    @Override
    public void execute() {
        if (context.isCharging()) {
            zombie.getAttackBehavior().execute();
            return;
        }

        if (canSeePlantIn4Tiles() && GameSession.getInstance().getCurrentSun() >= STEAL_AMOUNT) {
            zombie.getAttackBehavior().execute();
        } else {
            zombie.move();
        }
    }

    private boolean canSeePlantIn4Tiles() {
        GameSession session = GameSession.getInstance();
        int zRow = zombie.getRow();
        int zCol = zombie.getCol();

        for (Plant p : session.getArena().getActivePlants()) {
            int pRow = p.getPlacedTile().getRow();
            int pCol = p.getPlacedTile().getCol();

            if (pRow == zRow && pCol <= zCol && pCol >= zCol - 4) {
                return true;
            }
        }
        return false;
    }
}
