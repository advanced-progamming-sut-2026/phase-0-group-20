package models.fields.obstacle;

import models.game.GameSession;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

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
                            .message("Grave destroyed at row: " + row + ", col: " + col)
                            .build());

            GameSession session = GameSession.getInstance();
            if (graveStone.hasSun()) session.addSun(50);
            if (graveStone.hasPlantFood()) session.spawnPlantFood(row, col);

            removeGrave();
        }
    }
}
