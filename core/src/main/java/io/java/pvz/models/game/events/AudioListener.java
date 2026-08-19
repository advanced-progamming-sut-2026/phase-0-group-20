package io.java.pvz.models.game.events;

import io.java.pvz.controllers.AudioManager;
import io.java.pvz.views.sound.MusicType;

public class AudioListener implements GameEventListener {
    private final AudioManager audioManager = AudioManager.getInstance();


    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        switch (event) {
            case ENTERED_MENUS -> audioManager.playMusic(MusicType.MENU_BGM);
            case ENTERED_BIG_WAVE_BEACH ->  audioManager.playMusic(MusicType.BIG_WAVE_BEACH);
            case ENTERED_DARK_AGES ->   audioManager.playMusic(MusicType.DARK_AGES);
            case ENTERED_EGYPT -> audioManager.playMusic(MusicType.ANCIENT_EGYPT);
            case ENTERED_FROZEN_CAVES ->   audioManager.playMusic(MusicType.FROZEN_CAVES);
            case ENTERED_ZEN_GARDEN ->   audioManager.playMusic(MusicType.ZEN_GARDEN);
        }
    }
}
