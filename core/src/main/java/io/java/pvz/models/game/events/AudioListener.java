package io.java.pvz.models.game.events;

import io.java.pvz.controllers.AudioManager;

public class AudioListener implements GameEventListener {
    private final AudioManager audioManager = AudioManager.getInstance();


    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        switch (event) {
            default -> {
                return;
            }
        }
    }
}
