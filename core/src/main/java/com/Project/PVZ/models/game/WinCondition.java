package com.Project.PVZ.models.game;

import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventMessenger;
import com.Project.PVZ.models.game.events.GameEventPayload;

public interface WinCondition {
    boolean isWon(GameSession session);

    default void notify(String message) {
        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(message)
                        .build());
    }
}
