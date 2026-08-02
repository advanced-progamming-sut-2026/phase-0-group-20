package com.Project.PVZ.models.game.events;

public interface GameEventListener {
    void onEvent(GameEvent event, GameEventPayload payload);
}
