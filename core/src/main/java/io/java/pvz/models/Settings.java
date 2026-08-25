package io.java.pvz.models;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Settings {
    private static Settings instance;
    private Preferences prefs;

    private float musicVolume;
    private float sfxVolume;
    private int difficulty;
    private int progressSpeed;
    private boolean isGrid;
    private boolean isDebug;

    private Settings() {
        if (Gdx.app != null) {
            prefs = Gdx.app.getPreferences("pvz_settings");

            musicVolume = prefs.getFloat("musicVolume", 5f);
            sfxVolume = prefs.getFloat("sfxVolume", 5f);
            isGrid = prefs.getBoolean("isGrid", false);
            isDebug = prefs.getBoolean("isDebug", false);
            progressSpeed = prefs.getInteger("progressSpeed", 1);
        } else {
            prefs = null;
            musicVolume = 5f;
            sfxVolume = 5f;
            isGrid = false;
            isDebug = false;
            progressSpeed = 1;
        }
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        instance.setDifficulty((App.getActiveUser()) == null ? 3 : App.getActiveUser().getDifficulty());
        return instance;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
        if (prefs != null) {
            prefs.putFloat("musicVolume", musicVolume);
            prefs.flush();
        }
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
        if (prefs != null) {
            prefs.putFloat("sfxVolume", sfxVolume);
            prefs.flush();
        }
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
        if(App.getActiveUser() != null) {
            App.getActiveUser().setDifficulty(difficulty);
        }
    }

    public boolean isGrid() {
        return isGrid;
    }

    public void setGrid(boolean grid) {
        this.isGrid = grid;
        if (prefs != null) {
            prefs.putBoolean("isGrid", grid);
            prefs.flush();
        }
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean debug) {
        this.isDebug = debug;
        if (prefs != null) {
            prefs.putBoolean("isDebug", debug);
            prefs.flush();
        }
    }

    public int getProgressSpeed() {
        return progressSpeed;
    }

    public void setProgressSpeed(int progressSpeed) {
        this.progressSpeed = progressSpeed;
        if (prefs != null) {
            prefs.putInteger("progressSpeed", progressSpeed);
            prefs.flush();
        }
    }

    public float getZombieHealthMultiplier() {
        return 1.0f + (difficulty - 3) * 0.2f;
    }

    public float getZombieDamageMultiplier() {
        return 1.0f + (difficulty - 3) * 0.2f;
    }

    public float getZombieCostMultiplier() {
        return 1.0f - (difficulty - 3) * 0.1f;
    }

    public float getSunSpawnIntervalMultiplier() {
        return 1.0f + (difficulty - 3) * 0.25f;
    }

    public float getGameSpeedMultiplier() {
        return 1.0f + (difficulty - 3) * 0.15f;
    }
}
