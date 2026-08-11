package io.java.pvz.models.entities.projectiles;

import io.java.pvz.models.enums.plants.ProjectileType;

public final class ProjectileTuning {
    private ProjectileTuning() {}


    public static final float PEA_SPEED_TILES_PER_SEC = 5.5f;

    public static final float SPIKE_SPEED_TILES_PER_SEC = 6.0f;

    public static final float FUME_SPEED_TILES_PER_SEC = 5.0f;

    public static final float LOB_SPEED_TILES_PER_SEC = 4.0f;

    public static final float LOB_ARC_HEIGHT_TILES = 2.2f;

    public static final float HOMING_SPEED_TILES_PER_SEC = 5.0f;

    public static final float LOST_TARGET_FALLBACK_SPEED_TILES_PER_SEC = 4.0f;

    public static final float GRAPE_SPEED_TILES_PER_SEC = 6.5f;

    public static final float BOWLING_SPEED_TILES_PER_SEC = 5.0f;

    public static final int VOLLEY_STAGGER_TICKS = 2; // 0.2s at TimeManager.TICKS_PER_SECOND = 10

    public static float speedFor(ProjectileType type) {
        if (type == null) return PEA_SPEED_TILES_PER_SEC;
        return switch (type) {
            case SPIKE -> SPIKE_SPEED_TILES_PER_SEC;
            case FUME -> FUME_SPEED_TILES_PER_SEC;
            case GRAPE -> GRAPE_SPEED_TILES_PER_SEC;
            default -> PEA_SPEED_TILES_PER_SEC;
        };
    }
}
