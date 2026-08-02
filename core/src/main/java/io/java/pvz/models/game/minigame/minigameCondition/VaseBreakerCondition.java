package io.java.pvz.models.game.minigame.minigameCondition;

import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.fields.tiles.VaseTile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.WinCondition;

public class VaseBreakerCondition implements WinCondition {

    @Override
    public boolean isWon(GameSession session) {
        if (!session.getArena().getActiveZombies().isEmpty())
            return false;

        for (int r = 0; r < session.getArena().getRows(); r++) {
            for (int c = 0; c < session.getArena().getCols(); c++) {
                Tile tile = session.getArena().getTile(r, c);
                if (tile instanceof VaseTile)
                    return false;
            }
        }
        return true;
    }
}
