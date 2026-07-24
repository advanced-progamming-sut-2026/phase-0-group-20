package models.game.adventure;

public enum SeasonType {
    ANCIENT_EGYPT,
    FROZEN_CAVES,
    DARK_AGES,
    BIG_WAVE_BEACH,
    MINI_GAME;


    public String getName() {
        return this.name().replace("_", " ").toLowerCase();
    }
}
