package io.java.pvz.models;


import static io.java.pvz.models.enums.PhysicalConstants.TILE_UNIT_LENGTH;
import static io.java.pvz.models.enums.PhysicalConstants.GRID_OFFSET_X;
import static io.java.pvz.models.enums.PhysicalConstants.GRID_OFFSET_Y;

public final class Position {

    private float x;
    private float y;
    private int col;
    private int row;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
        updateGridFromPixels();
    }


    public Position(int col, int row) {
        this.row = row;
        this.col = col;
        updatePixelsFromGrid();
    }

    private void updateGridFromPixels() {
        this.col = (int) Math.floor((this.x - GRID_OFFSET_X) / TILE_UNIT_LENGTH);
        this.row = (int) Math.floor((this.y - GRID_OFFSET_Y) / TILE_UNIT_LENGTH);
    }

    private void updatePixelsFromGrid() {
        this.x = (this.col * TILE_UNIT_LENGTH) + (TILE_UNIT_LENGTH / 2f) + GRID_OFFSET_X;
        this.y = (this.row * TILE_UNIT_LENGTH) + (TILE_UNIT_LENGTH / 2f) + GRID_OFFSET_Y;
    }

    public void setPosition(float x, float y) {
        setX(x);
        setY(y);
    }

    public void setPosition(int col, int row) {
        setRow(row);
        setCol(col);
    }

    public void moveX(float dx) {
        this.x += dx;
        updateGridFromPixels();
    }

    public void moveY(float dy) {
        this.y += dy;
        updateGridFromPixels();
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
        updateGridFromPixels();
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
        updateGridFromPixels();
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
        updatePixelsFromGrid();
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
        updatePixelsFromGrid();
    }


    @Override
    public String toString() {
        return "Position{" +
            "x=" + x +
            ", y=" + y +
            ", row=" + row +
            ", col=" + col +
            '}';
    }

}
