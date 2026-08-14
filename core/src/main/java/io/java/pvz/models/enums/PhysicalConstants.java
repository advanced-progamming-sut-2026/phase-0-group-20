package io.java.pvz.models.enums;

public enum PhysicalConstants {
    ;
    public static final int TILE_UNIT_LENGTH = 10;
    public static final float TILE_WIDTH = 115f; // be in adada dast nazanin pare shodam sareshoon
    public static final float TILE_HEIGHT = 136f;
    public static final float GRID_START_X = 750;
    public static final float GRID_START_Y = 115f;

    public static final float SPEED_SCALE_RATIO = TILE_WIDTH / TILE_UNIT_LENGTH; // will be used instead of tile unit length
    public static final int ROWS  = 5;
    public static final int COLUMNS  = 9;
}
