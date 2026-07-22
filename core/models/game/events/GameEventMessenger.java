package models.game.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameEventMessenger {
    private static GameEventMessenger instance;
    private final Map<GameEvent, List<GameEventListener>> listeners;

    private GameEventMessenger() {
        listeners = new HashMap<>();
        for (GameEvent event : GameEvent.values()) {
            listeners.put(event, new ArrayList<>());
        }
    }

    public static GameEventMessenger getInstance() {
        if (instance == null) {
            instance = new GameEventMessenger();
        }
        return instance;
    }

    public void addListener(GameEvent event, GameEventListener listener) {
        listeners.get(event).add(listener);
    }

    public void removeListener(GameEvent event, GameEventListener listener) {
        listeners.get(event).remove(listener);
    }

    public void dispatch(GameEvent event, GameEventPayload payload) {
        List<GameEventListener> eventListeners = listeners.get(event);

        if (eventListeners != null) {

            List<GameEventListener> safeCopy =new ArrayList<>(eventListeners);

            for (GameEventListener listener : safeCopy) {
                listener.onEvent(event, payload);
            }
        }
    }
}
