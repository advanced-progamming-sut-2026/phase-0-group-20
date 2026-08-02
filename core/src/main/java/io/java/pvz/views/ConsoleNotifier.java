package io.java.pvz.views;

import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;

public class ConsoleNotifier implements GameEventListener {

    public static void register() {
        ConsoleNotifier notifier = new ConsoleNotifier();
        for (GameEvent event : GameEvent.values()) {
            GameEventMessenger.getInstance().addListener(event, notifier);
        }
    }


    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (payload != null && payload.getMessage() != null) {
            System.out.println(payload.getMessage());
        }
    }
}
