package models.game;

import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

public interface WinCondition {
    boolean isWon(GameSession session);

    default void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }
}
