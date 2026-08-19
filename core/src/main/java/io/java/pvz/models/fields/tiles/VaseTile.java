package io.java.pvz.models.fields.tiles;

import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public interface VaseTile {
    VaseInside breakVase();

    boolean isBroken();

    default void notify(String message) {
//        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
//                new GameEventPayload.Builder(GameEvent.NOTIFY)
//                        .message(message)
//                        .build());
    }
}
