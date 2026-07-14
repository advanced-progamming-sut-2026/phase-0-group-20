package models.game;

import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.fields.LawnMower;
import models.fields.tiles.NormalTile;
import models.fields.tiles.Tile;

import java.util.ArrayList;
import java.util.List;

public class Arena {
    private final int ROWS = 5;
    private final int COLS = 9;
    private final List<Plant> activePlants;// these are the plants that are already in the arena.(placed and not dead)
    private final List<Zombie> activeZombies;//these are the zombies that are already in the arena and moving/attacking.
    private final LawnMower[] lawnMowers;
    private Tile[][] tiles;
    private List<Sun> activeSuns = new ArrayList<>();

    public Arena() {
        this.tiles = new Tile[ROWS][COLS];
        for (int r = 0; r < ROWS; r++)
            for (int c = 0; c < COLS; c++)
                this.tiles[r][c] = new NormalTile(r, c);

        this.activePlants = new ArrayList<>();
        this.activeZombies = new ArrayList<>();
        this.lawnMowers = new LawnMower[ROWS];

        for (int i = 0; i < ROWS; i++) {
            lawnMowers[i] = new LawnMower(i, this);
            GameSession.getInstance().getTimeManager().registerNewTicker(lawnMowers[i]);
        }
    }

    public void addZombie(Zombie zombie) {
        activeZombies.add(zombie);
    }

    public void addPlant(Plant plant) {
        activePlants.add(plant);
    }

    public List<Zombie> getZombiesInRadius(int column, int lane, double radius) {
        List<Zombie> result = new ArrayList<>();
        for (Zombie zombie : activeZombies) {
            if (zombie.isDead()) continue;
            radius *= PhysicalConstants.TILE_UNIT_LENGTH;
            double dx = zombie.getX() - column * PhysicalConstants.TILE_UNIT_LENGTH;
            double dy = zombie.getY() - lane * PhysicalConstants.TILE_UNIT_LENGTH;
            if (Math.sqrt(dx * dx + dy * dy) <= radius) result.add(zombie);
        }
        return result;
    }

    public List<Zombie> zombieInRow(int row) {
        List<Zombie> zombies = new ArrayList<>();
        for (Zombie z : activeZombies) {
            if (z.getRow() == row) {
                zombies.add(z);
            }
        }
        return zombies;
    }

    public Plant nearestPlantInRow(Zombie zombie, int row) {
        Plant target = null;
        int maxCol = -1;

        for (Plant p : GameSession.getInstance().getArena().getActivePlants()) {
            if (p.getPlacedTile() != null && p.getPlacedTile().getRow() == row) {
                int pCol = p.getPlacedTile().getCol();
                if (pCol <= zombie.getCol() && pCol > maxCol) {
                    maxCol = pCol;
                    target = p;
                }
            }
        }
        return target;
    }

    public Sun getSunInCoordinate(int col, int row) {
        for (Sun sun : activeSuns) {
            if (!sun.isCollected() && sun.getCol() == col && sun.getRow() == row) return sun;
        }
        return null;
    }

    public void setZombiesOnTiles() {
        for (Zombie zombie : activeZombies) {
            zombie.setTile(this.tiles[zombie.getRow()][zombie.getCol()]);
        }
    }


    public List<Plant> getActivePlants() {
        return activePlants;
    }

    public List<Zombie> getActiveZombies() {
        return activeZombies;
    }

    public int getRows() {
        return ROWS;
    }

    public int getCols() {
        return COLS;
    }

    public List<Sun> getActiveSuns() {
        return activeSuns;
    }

    public void addSun(Sun sun) {
        activeSuns.add(sun);
    }

    public LawnMower[] getLawnMowers() {
        return lawnMowers;
    }

    public Tile[][] getTiles() {
        return tiles;
    }

    public Tile getTile(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return null;
        return tiles[row][col];
    }

    public void changeTile(int row, int col, Tile tile) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;
        tiles[row][col] = tile;
    }

}
