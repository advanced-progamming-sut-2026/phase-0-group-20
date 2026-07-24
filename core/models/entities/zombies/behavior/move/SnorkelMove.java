package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.context.SnorkelContext;
import models.fields.tiles.Tile;
import models.fields.tiles.WaterTile;
import models.game.GameSession;

public class SnorkelMove implements MoveBehavior {
    private final Zombie zombie;
    private final SnorkelContext context;

    public SnorkelMove(Zombie zombie, SnorkelContext context) {
        this.zombie = zombie;
        this.context = context;
    }

    @Override
    public void execute() {
        Tile currentTile = zombie.getTile();

        if (isWaterTile(currentTile)) {
            if (!context.isSubmerged()) {
                context.setSubmerged(true);
                GameSession.notify("Snorkel Zombie dove underwater!");
            }
        } else {
            if (context.isSubmerged()) {
                context.setSubmerged(false);
                GameSession.notify("Snorkel Zombie is walking on land.");
            }
        }
        zombie.moveForward();
    }

    private boolean isWaterTile(Tile tile) {
        return tile instanceof WaterTile;
    }
}
