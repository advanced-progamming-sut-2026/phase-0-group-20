package io.java.pvz.models.entities.obstacle;

import io.java.pvz.models.App;
import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.SunType;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public interface GraveHolder {

    GraveStone getGraveStone();

    void removeGrave();

    default void takeDamage(int damage, int row, int col) {
        GraveStone graveStone = getGraveStone();
        if (graveStone == null) return;
        graveStone.takeDamage(damage);

        if (graveStone.getHp() <= 0) {
            GameSession session = GameSession.getInstance();

            if (graveStone.hasSun()) {
                Sun sun = new Sun(SunType.NORMAL_SUN, col, row);
                sun.setFalling(false);
                session.getArena().addSun(sun);
                session.getTimeManager().registerNewTicker(sun);
            }

            if (graveStone.hasPlantFood()) {
                App.getActiveUser().addPlantFoodCount(1);
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("new plant food added to your bank")
                        .build()
                );
            }

            removeGrave();
        }
    }
}
