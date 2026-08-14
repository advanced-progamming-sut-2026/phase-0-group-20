package io.java.pvz.models.entities.zombies.behavior.move;

import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.context.SnorkelContext;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.fields.tiles.WaterTile;
import io.java.pvz.models.game.GameSession;

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
        zombie.move();
    }

    private boolean isWaterTile(Tile tile) {
        return tile instanceof WaterTile;
    }
}
