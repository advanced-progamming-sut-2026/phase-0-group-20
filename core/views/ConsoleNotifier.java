package views;

import models.game.events.GameEvent;
import models.game.events.GameEventListener;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;

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
