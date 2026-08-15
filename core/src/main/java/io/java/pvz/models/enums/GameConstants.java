package io.java.pvz.models.enums;

import io.java.pvz.models.timeManager.TimeManager;

public enum GameConstants {
    ;
    public static final int BASE_WAVE_COST = 1000;
    public static final int SPAWN_IN_WAVE_INTERVAL = 3 * TimeManager.TICKS_PER_SECOND;
    public static final int LEVEL_EFFECTS_SCALE = 5;
    public static final int CHAPTER_COUNT = 4;

}
