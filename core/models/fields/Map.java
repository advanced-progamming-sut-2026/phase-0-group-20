package models.fields;

import models.fields.tiles.Tile;

public class Map {
    // map is ALWAYS created before the mission start
    private int rows;
    private int columns;
    private Tile[][] tiles;
    public Map(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        Tile[][] tiles = new Tile[rows][columns];
    }

}
