package com.Project.PVZ.models.game.minigame.minigameCondition;

import com.Project.PVZ.models.fields.tiles.Tile;
import com.Project.PVZ.models.fields.tiles.VaseTile;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.WinCondition;

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
