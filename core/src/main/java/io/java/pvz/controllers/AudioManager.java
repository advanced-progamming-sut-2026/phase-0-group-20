package io.java.pvz.controllers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import io.java.pvz.models.App;
import io.java.pvz.views.sound.MusicType;
import io.java.pvz.views.sound.SfxType;

public class AudioManager {
    private static AudioManager instance;
    private AssetManager assetManager;

    private Music currentTrack;
    private MusicType currentTrackType;
    private float sfxVolume = App.getSettings().getSfxVolume();
    private float musicVolume = App.getSettings().getMusicVolume();

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private AudioManager() {
    }

    public void init(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public long loopSfx(SfxType type) {
        Sound sound = getSound(type);
        if (sound != null) {
            return sound.loop(sfxVolume);
        }
        return -1;
    }

    public void playMusic(MusicType type) {
        if (currentTrackType == type && currentTrack != null && currentTrack.isPlaying()) {
            return;
        }

        stopMusic();

        currentTrackType = type;
        currentTrack = getMusic(type);

        if (currentTrack != null) {
            currentTrack.setLooping(true);
            currentTrack.setVolume(musicVolume);
            currentTrack.play();
        }
    }

    public void playSfx(SfxType type) {
        Sound sound = getSound(type);
        if (sound != null) {
            float pitch = 0.9f + (float) (Math.random() * 0.2f);
            sound.play(sfxVolume, pitch, 0);
        }
    }

    public void playSfx(SfxType type, float xPosition, float screenWidth) {
        Sound sound = getSound(type);
        if (sound != null) {
            float pan = (xPosition / screenWidth) * 2f - 1f;
            sound.play(sfxVolume, 1f, pan);
        }
    }

    public void stopMusic() {
        if (currentTrack != null) currentTrack.stop();
        currentTrack = null;
    }

    public void stopSfx(SfxType type, long soundId) {
        if (soundId == -1) return;
        Sound sound = getSound(type);
        if (sound != null) {
            sound.stop(soundId);
        }
    }

    public void changeSfxVolume(float volume) {
        sfxVolume = volume/10f;
    }

    public void changeMusicVolume(float volume) {
        musicVolume = volume;
        if (currentTrack != null) {
            currentTrack.setVolume(musicVolume/10);
        }
    }

    private Sound getSound(SfxType type) {
        if (assetManager != null && assetManager.isLoaded(type.getPath(), Sound.class)) {
            return assetManager.get(type.getPath(), Sound.class);
        }
        return null;
    }

    private Music getMusic(MusicType type) {
        if (assetManager != null && assetManager.isLoaded(type.getPath(), Music.class)) {
            return assetManager.get(type.getPath(), Music.class);
        }
        return null;
    }

    public void dispose() {
        currentTrack = null;
        currentTrackType = null;
    }
}
