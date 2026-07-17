package models.game;

import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.enums.PhysicalConstants;
import models.fields.LawnMower;
import models.fields.tiles.NormalTile;
import models.fields.tiles.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Arena {
    private final int ROWS = 5;
    private final int COLS = 9;
    private final List<Plant> activePlants;// these are the plants that are already in the arena.(placed and not dead)
    private final List<Zombie> activeZombies;//these are the zombies that are already in the arena and moving/attacking.
    private final List<Projectile> activeProjectiles = new ArrayList<>();
    private final LawnMower[] lawnMowers;
    private Tile[][] tiles;
    private List<Sun> activeSuns = new ArrayList<>();
    private Wave currentActiveWave;
    private final List<Wave> spawnedWaves = new ArrayList<>();

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


    public Zombie getNearestZombie(int column, int lane) {
        if (activeZombies.isEmpty()) return null;
        for (float i = 0; i < PhysicalConstants.TILE_UNIT_LENGTH * COLS; i += 0.05f) {
            List<Zombie> zombies = getZombiesInRadius(column, lane, i);
            if (!zombies.isEmpty()) return zombies.get(0);
        }
        return null;
    }

    public Zombie getCollidingEnemyZombie(Zombie attacker, float collisionRadius) {
        for (Zombie z : activeZombies) {
            if (z == attacker || z.isDead()) continue;

            if (attacker.isHypnotized() != z.isHypnotized()) {

                if (z.getRow() == attacker.getRow()) {
                    float distance = Math.abs(attacker.getX() - z.getX());
                    if (distance <= collisionRadius) {
                        return z;
                    }
                }
            }
        }
        return null;
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


    public List<Tile> getRandomEmptyTiles(int count) {
        List<Tile> emptyTiles = new ArrayList<>();
        for (Tile[] row : tiles)
            for (Tile tile : row)
                if (tile.getPlants().isEmpty() && getZombiesOnTile(tile).isEmpty())
                    emptyTiles.add(tile);

        Collections.shuffle(emptyTiles);
        return emptyTiles.subList(0, Math.min(count, emptyTiles.size()));//Hossein karo dary ya chi?
    }

    public List<Zombie> getZombiesOnTile(Tile tile) {
        List<Zombie> zombies = new ArrayList<>();
        for (Zombie z : activeZombies)
            if (z.getRow() == tile.getRow() && z.getCol() == tile.getCol())
                zombies.add(z);

        return zombies;
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

    public void addProjectile(Projectile p) {
        activeProjectiles.add(p);
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

    public List<Projectile> getActiveProjectiles() {
        return activeProjectiles;
    }

    public Tile getTile(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return null;
        return tiles[row][col];
    }

    public void changeTile(int row, int col, Tile tile) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return;
        tiles[row][col] = tile;
    }

    public Wave getCurrentActiveWave() {
        return currentActiveWave;
    }

    public void setCurrentActiveWave(Wave wave) {
        this.currentActiveWave = wave;
        if (wave != null) {
            this.spawnedWaves.add(wave);
        }
    }

    public List<Wave> getSpawnedWaves() {
        return spawnedWaves;
    }

}
