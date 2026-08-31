package io.java.pvz.models.entities.zombies.behavior.attack;

import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public interface AttackBehavior {
    void execute();

    default void notify(String message) {
        //di di chi shod?
    }
}
