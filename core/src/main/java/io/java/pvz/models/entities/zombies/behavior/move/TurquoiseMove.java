package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.context.TurquoiseContext;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.game.GameSession;

public class TurquoiseMove implements MoveBehavior {
    private final Zombie zombie;
    private final TurquoiseContext context;

    public TurquoiseMove(Zombie zombie, TurquoiseContext context) {
        this.zombie = zombie;
        this.context = context;
    }

    @Override
    public void execute() {
        if (context.isCharging()) {
            zombie.getAttackBehavior().execute();
        } else {
            if (canSeePlantIn4Tiles()) {
                context.startCharging();
                zombie.getAttackBehavior().execute();
            } else {
                zombie.moveForward();
            }
        }
    }

    private boolean canSeePlantIn4Tiles() {
        GameSession session = GameSession.getInstance();
        int zRow = zombie.getRow();
        int zCol = (int) (zombie.getX() / PhysicalConstants.TILE_UNIT_LENGTH);

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
