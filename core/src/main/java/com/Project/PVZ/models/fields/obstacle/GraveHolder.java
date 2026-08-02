package com.Project.PVZ.models.fields.obstacle;

import com.Project.PVZ.models.App;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventMessenger;
import com.Project.PVZ.models.game.events.GameEventPayload;

public interface GraveHolder {

    GraveStone getGraveStone();

    void removeGrave();

    default void takeDamage(int damage, int row, int col) {
        GraveStone graveStone = getGraveStone();
        if (graveStone == null) return;
        graveStone.takeDamage(damage);

        if (graveStone.getHp() <= 0) {
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                            .message("Grave destroyed at row: " + (row + 1) + ", col: " + (col + 1))
                            .build());

            GameSession session = GameSession.getInstance();
            if (graveStone.hasSun()) session.addSun(50);
            if (graveStone.hasPlantFood()) App.getActiveUser().addPlantFoodCount(1);

            removeGrave();
        }
    }
}
