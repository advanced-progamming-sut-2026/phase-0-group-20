package models;


import static models.enums.PhysicalConstants.TILE_UNIT_LENGTH;

public final class
Position {

    private float x;
    private float y;
    private int col;
    private int row;

    public Position(float x, float y) {
        this.x = x;
        this.y = y;
        this.col = (int) (x / TILE_UNIT_LENGTH);

        this.row = (int) (y / TILE_UNIT_LENGTH);
    }


    public Position(int col, int row) {
        this.row = row;
        this.col = col;
        this.y = row * TILE_UNIT_LENGTH + TILE_UNIT_LENGTH / 2f;
        this.x = col * TILE_UNIT_LENGTH + TILE_UNIT_LENGTH / 2f;
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
        this.col = (int) (x / TILE_UNIT_LENGTH);
    }

    public void moveY(float dy) {
        this.y += dy;
        this.row = (int) (y / TILE_UNIT_LENGTH);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setX(float x) {
        this.x = x;
        this.col = (int) (x / TILE_UNIT_LENGTH);
    }

    public void setY(float y) {
        this.y = y;
        this.row = (int) (y / TILE_UNIT_LENGTH);
    }

    public void setRow(int row) {
        this.row = row;
        this.y = row * TILE_UNIT_LENGTH + TILE_UNIT_LENGTH / 2f;
    }

    public void setCol(int col) {
        this.col = col;
        this.x = col * TILE_UNIT_LENGTH + TILE_UNIT_LENGTH / 2f;
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
