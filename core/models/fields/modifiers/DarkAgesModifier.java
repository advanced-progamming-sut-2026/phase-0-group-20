package models.fields.modifiers;

import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieFactory;
import models.entities.zombies.ZombieType;
import models.fields.obstacle.GraveStone;
import models.fields.tiles.GraveStoneTile;
import models.fields.tiles.NecromanceTile;
import models.fields.tiles.NormalTile;
import models.fields.tiles.Tile;
import models.game.Arena;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class DarkAgesModifier implements SeasonModifier {

    private static final double GRAVE_SPAWN_CHANCE = 0.6;
    private static final double SUN_GRAVE_CHANCE = 0.25;
    private static final double PLANT_FOOD_GRAVE_CHANCE = 0.15;

    private final Random rand = new Random();

    @Override
    public void onCurrentLevelStart() {
        Arena arena = GameSession.getInstance().getArena();
        setupNecromanceTiles(arena);
    }

    @Override
    public void onWaveStart(Wave wave) {
        Arena arena = GameSession.getInstance().getArena();

        spawnRandomGraves(arena);
        raiseZombiesFromGraves(arena);
    }

    @Override
    public void onZombieSpawn(Zombie zombie, Arena arena) {
        // zombies enter normally in the Dark Ages
    }

    @Override
    public void updateEnvironment(int currentTick, Arena arena) {
        // it is night: no sun falls from the sky (handled by Level.skySunFalls())
    }

    private void setupNecromanceTiles(Arena arena) {
        int rows = arena.getRows();
        int cols = arena.getCols();


        int numberOfTiles = (rand.nextInt(2) + 2) + getCurrentLevelNumber();

        int placed = 0;

        long remainTiles = Arrays.stream(arena.getTiles())
                .flatMap(Arrays::stream)
                .filter(t -> t instanceof NormalTile && t.getPlants().isEmpty() && t.getCol() >= cols / 2)
                .count();

        while (placed < numberOfTiles && remainTiles > 0) {
            int randomRow = rand.nextInt(rows);
            int randomCol = rand.nextInt(cols / 2) + (cols / 2);

            Tile tile = arena.getTile(randomRow, randomCol);
            if (tile instanceof NormalTile && tile.getPlants().isEmpty()) {
                arena.changeTile(randomRow, randomCol, new NecromanceTile(randomRow, randomCol));
                placed++;
                remainTiles--;
            }
        }
        notify(numberOfTiles + " cursed necromancy grounds lie hidden on this map...");
    }

    private void spawnRandomGraves(Arena arena) {
        double currentSpawnChance = Math.min(0.9, GRAVE_SPAWN_CHANCE + 0.5 * getCurrentLevelNumber());
        if (rand.nextDouble() >= currentSpawnChance) return;


        int rows = arena.getRows();
        int cols = arena.getCols();
        int gravesToSpawn = rand.nextInt(2) + 1 + getCurrentLevelNumber();

        long remainTiles = Arrays.stream(arena.getTiles())
                .flatMap(Arrays::stream)
                .filter(t -> t instanceof NormalTile && t.getPlants().isEmpty() && t.getCol() >= cols / 2)
                .count();

        while (gravesToSpawn > 0 && remainTiles > 0) {
            int randomRow = rand.nextInt(rows);
            int randomCol = rand.nextInt(cols / 2) + (cols / 2);

            Tile tile = arena.getTile(randomRow, randomCol);
            // a grave can never rise where a plant is already planted
            if (tile == null || !tile.getPlants().isEmpty()) continue;

            GraveStone graveStone = rollGraveContents();

            if (tile instanceof NecromanceTile necromanceTile) {
                if (necromanceTile.canZombieEmerge()) continue; // already holds a grave
                necromanceTile.setGraveStone(graveStone);
            } else if (tile instanceof NormalTile) {
                arena.changeTile(randomRow, randomCol, new GraveStoneTile(randomRow, randomCol, graveStone));
            } else {
                continue;
            }

            announceGrave(randomRow, randomCol, graveStone);
            gravesToSpawn--;
            remainTiles--;
        }
    }

    private GraveStone rollGraveContents() {
        double roll = rand.nextDouble();
        if (roll < SUN_GRAVE_CHANCE) return new GraveStone(true, false);
        if (roll < SUN_GRAVE_CHANCE + PLANT_FOOD_GRAVE_CHANCE) return new GraveStone(false, true);
        return new GraveStone(false, false);
    }

    private void announceGrave(int row, int col, GraveStone graveStone) {
        String contents = "";
        if (graveStone.hasSun()) contents = " It holds 50 sun!";
        else if (graveStone.hasPlantFood()) contents = " It holds a plant food!";
        notify("A grave emerged from the ground at row " + (row + 1) + ", col " + (col + 1) + "!" + contents);
    }

    private void raiseZombiesFromGraves(Arena arena) {
        List<NecromanceTile> hauntedTiles = new ArrayList<>();
        for (Tile[] row : arena.getTiles())
            for (Tile tile : row)
                if (tile instanceof NecromanceTile necromanceTile && necromanceTile.canZombieEmerge())
                    hauntedTiles.add(necromanceTile);

        for (NecromanceTile tile : hauntedTiles) {
            Zombie zombie = ZombieFactory.create(ZombieType.NORMAL, tile.getRow());
            tile.spawnZombieFromBelow(zombie);
            notify("A zombie crawled out from under the grave at row "
                    + (tile.getRow() + 1) + ", col " + (tile.getCol() + 1) + "!");
        }
    }
}
