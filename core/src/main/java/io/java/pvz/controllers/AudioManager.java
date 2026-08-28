package io.java.pvz.controllers;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;
import io.java.pvz.models.App;
import io.java.pvz.views.sound.MusicType;
import io.java.pvz.views.sound.SfxType;

public class AudioManager {
    private static final float DEFAULT_FADE_DURATION = 1.5f;
    private static final float FADE_STEP = 0.05f;

    private static AudioManager instance;
    private AssetManager assetManager;

    private Music currentTrack;
    private MusicType currentTrackType;
    private float sfxVolume = App.getSettings().getSfxVolume();
    private float musicVolume = App.getSettings().getMusicVolume();

    private Timer.Task fadeTask;
    private Music fadeOutTrack;

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
        playMusic(type, DEFAULT_FADE_DURATION);
    }

    public void playMusic(MusicType type, float fadeDuration) {
        if (currentTrackType == type && currentTrack != null && currentTrack.isPlaying()) {
            return;
        }
        cancelFade();
        Music outgoing = currentTrack;
        Music incoming = getMusic(type);
        currentTrackType = type;
        currentTrack = incoming;
        if (incoming == null) {
            if (outgoing != null) outgoing.stop();
            return;
        }
        incoming.setLooping(true);

        float normalizedVolume = musicVolume / 10f;
        if (fadeDuration <= 0f) {
            if (outgoing != null) outgoing.stop();
            incoming.setVolume(normalizedVolume);
            incoming.play();
            return;
        }
        incoming.setVolume(0f);
        incoming.play();
        fadeOutTrack = outgoing;

        final Music fadingIn = incoming;
        fadeTask = new Timer.Task() {
            private float elapsed = 0f;
            @Override
            public void run() {
                elapsed += FADE_STEP;
                float progress = Math.min(elapsed / fadeDuration, 1f);

                fadingIn.setVolume(normalizedVolume * progress);
                if (fadeOutTrack != null) {
                    fadeOutTrack.setVolume(normalizedVolume * (1f - progress));
                }
                if (progress >= 1f) {
                    if (fadeOutTrack != null) {
                        fadeOutTrack.stop();
                        fadeOutTrack = null;
                    }
                    cancel();
                    fadeTask = null;
                }
            }
        };
        Timer.schedule(fadeTask, FADE_STEP, FADE_STEP);
    }

    private void cancelFade() {
        if (fadeTask != null) {
            fadeTask.cancel();
            fadeTask = null;
        }
        if (fadeOutTrack != null) {
            fadeOutTrack.stop();
            fadeOutTrack = null;
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
        cancelFade();
        if (currentTrack != null) currentTrack.stop();
        currentTrack = null;
        currentTrackType = null;
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
        cancelFade();
        if (currentTrack != null) currentTrack.stop();
        currentTrack = null;
        currentTrackType = null;
    }
}
