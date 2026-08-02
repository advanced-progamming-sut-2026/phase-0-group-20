package com.Project.PVZ.views;

import com.Project.PVZ.models.game.events.GameEvent;
import com.Project.PVZ.models.game.events.GameEventListener;
import com.Project.PVZ.models.game.events.GameEventMessenger;
import com.Project.PVZ.models.game.events.GameEventPayload;

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
