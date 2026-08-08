package io.java.pvz.models.enums;

public enum PhysicalConstants {
    ;
    public static final int TILE_UNIT_LENGTH = 10;
    public static final float TILE_WIDTH = 115f; // be in adada dast nazanin pare shodam sareshoon
    public static final float TILE_HEIGHT = 136f;
    public static final float GRID_START_X = 750;
    public static final float GRID_START_Y = 115f;
    public static final int GRID_OFFSET_X = 100;
    public static final int GRID_OFFSET_Y = 100;

    public static final float SPEED_SCALE_RATIO = TILE_WIDTH / 10f; // will be used instead of tile unit length
}
