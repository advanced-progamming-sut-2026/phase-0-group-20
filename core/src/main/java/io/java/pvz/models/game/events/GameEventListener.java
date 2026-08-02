package io.java.pvz.models.game.events;

public interface GameEventListener {
    void onEvent(GameEvent event, GameEventPayload payload);
}
