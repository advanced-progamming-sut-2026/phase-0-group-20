package models.entities.zombies.behavior.move;

import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.GameSession;

public class TurquoiseMove implements MoveBehavior {
    private final Zombie zombie;

    public TurquoiseMove(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public void execute() {
        GameSession session = GameSession.getInstance();
        boolean plantInRange = false;

        for (Plant p : session.getArena().getActivePlants()) {
            if (p.getPlacedTile() != null && p.getPlacedTile().getRow() == zombie.getRow()) {
                int distance = zombie.getCol() - p.getPlacedTile().getCol();
                if (distance >= 0 && distance <= 4) {
                    plantInRange = true;
                    break;
                }
            }
        }

        if (plantInRange) {
            zombie.setAttacking(true);
        } else {
            zombie.moveForward();
        }
    }
}
