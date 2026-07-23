package models;

public class Settings {
    private static Settings instance;
    private float musicVolume;
    private float sfxVolume;
    private int difficulty;

    private Settings() {


    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
            instance.musicVolume = 50.0f;
            instance.sfxVolume = 50.0f;
            instance.difficulty = 3;
        }
        return instance;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
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
        return 1.0f - (difficulty - 3) * 0.15f;
    }
}
