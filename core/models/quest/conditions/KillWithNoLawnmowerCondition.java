package models.quest.conditions;

import models.entities.zombies.Zombie;
import models.fields.LawnMower;
import models.fields.tiles.Tile;
import models.game.GameEvent;
import models.game.GameEventPayload;

public class KillWithNoLawnmowerCondition extends QuestCondition {

    public KillWithNoLawnmowerCondition(int amount) {
        targetProgress = amount;
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event == GameEvent.ZOMBIE_KILLED) {
            Zombie target = payload.getZombie();
            Tile killedTile = target.getTile();
            int x = killedTile.getCol();
            int y = killedTile.getRow();
            LawnMower mawer = payload.getArena().getLawnMowers()[y];
            if (x == 0) {
                if (mawer != null && mawer.isActivate()) {
                    currentProgress++;
                } else if (mawer == null) {
                    currentProgress++;
                }
            }
        }
    }
}
