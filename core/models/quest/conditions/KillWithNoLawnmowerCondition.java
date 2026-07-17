package models.quest.conditions;

import models.entities.zombies.Zombie;
import models.fields.LawnMower;
import models.fields.tiles.Tile;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class KillWithNoLawnmowerCondition extends QuestCondition {
    private  int column ;
    public KillWithNoLawnmowerCondition(int amount , int col) {
        targetProgress = amount;
        column = col;
    }
    public KillWithNoLawnmowerCondition() {}

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event == GameEvent.ZOMBIE_KILLED) {
            Zombie target = payload.getZombie();
            Tile killedTile = target.getTile();
            int x = killedTile.getCol();
            int y = killedTile.getRow();
            LawnMower mawer = payload.getArena().getLawnMowers()[y];
            if (x == column) {
                if (mawer != null && mawer.isActivate()) {
                    currentProgress++;
                } else if (mawer == null) {
                    currentProgress++;
                }
            }
        }
    }
}
