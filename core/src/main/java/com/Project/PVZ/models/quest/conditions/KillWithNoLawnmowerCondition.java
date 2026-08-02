package com.Project.PVZ.models.quest.conditions;

import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.fields.LawnMower;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventPayload;

public class KillWithNoLawnmowerCondition extends QuestCondition {
    private int column;

    public KillWithNoLawnmowerCondition(int amount, int col) {
        targetProgress = amount;
        column = col;
    }

    public KillWithNoLawnmowerCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();
        if (event == GameEvent.ZOMBIE_KILLED) {
            Zombie target = payload.getZombie();
            int x = target.getCol();
            int y = target.getRow();
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
