package models.fields.modifiers;

import models.entities.plants.Plant;
import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.enums.plants.PlantTag;
import models.fields.tiles.LowShoreTile;
import models.fields.tiles.Tile;
import models.fields.tiles.WaterTile;
import models.game.Arena;
import models.game.GameSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BigWaveModifier implements SeasonModifier {

    private static final int PERMANENT_WATER_COLS = 2; // the sea on the right edge
    private static final int MAX_WATER_COLS = 9; // the horizontal line the water never passes
    private static final int TIDE_INTERVAL_TICKS = 150;
    private static final double EMERGE_FROM_BELOW_CHANCE = 0.3;

    private final Random rand = new Random();
    private boolean isInitialized = false;
    private int currentWaterCols = PERMANENT_WATER_COLS;

    @Override
    public void onCurrentLevelStart() {
        Arena arena = GameSession.getInstance().getArena();
        setupShore(arena);
        isInitialized = true;
    }

    @Override
    public void onWaveStart(Wave wave) {
        Arena arena = GameSession.getInstance().getArena();
        changeTide(arena); // an incoming zombie wave always shifts the water level
    }

    @Override
    public void onZombieSpawn(Zombie zombie, Arena arena) {
        double currentEmergeChance = Math.min(0.8, EMERGE_FROM_BELOW_CHANCE + 0.05 * getCurrentLevelNumber());
        if (zombie == null || rand.nextDouble() >= currentEmergeChance) return;

        List<LowShoreTile> floodedShores = new ArrayList<>();
        for (Tile[] row : arena.getTiles())
            for (Tile tile : row)
                if (tile instanceof LowShoreTile shore && shore.canZombieEmerge())
                    floodedShores.add(shore);

        if (floodedShores.isEmpty()) return;

        LowShoreTile shore = floodedShores.get(rand.nextInt(floodedShores.size()));
        zombie.setRow(shore.getRow());
        zombie.setCol(shore.getCol());

        zombie.setSpawnEffect(Zombie.SpawnEffect.WATER_SPLASH); //for graphic phase

        notify("A zombie emerged from the water at row " + shore.getRow()
                + ", col " + shore.getCol() + "!");
    }

    @Override
    public void updateEnvironment(int currentTick, Arena arena) {
        if (!isInitialized) return;
        if (currentTick % TIDE_INTERVAL_TICKS == 0)
            changeTide(arena);
    }

    private void setupShore(Arena arena) {
        int rows = arena.getRows();
        int cols = arena.getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = cols - MAX_WATER_COLS; c < cols; c++) {
                if (c >= cols - PERMANENT_WATER_COLS)
                    arena.changeTile(r, c, new WaterTile(r, c));
                else
                    arena.changeTile(r, c, new LowShoreTile(r, c));
            }
        }
        notify("The " + PERMANENT_WATER_COLS + " rightmost columns are open sea;"
                + " the tide can reach up to column " + (arena.getCols() - MAX_WATER_COLS) + ".");
    }

    private void changeTide(Arena arena) {

        int levelEffect = getCurrentLevelNumber() - 1;
        int randomTide = rand.nextInt(MAX_WATER_COLS - PERMANENT_WATER_COLS + 1);
        int newWaterCols = Math.min(MAX_WATER_COLS, PERMANENT_WATER_COLS + randomTide + levelEffect);

        if (newWaterCols == currentWaterCols) return;

        if (newWaterCols > currentWaterCols)
            notify("The tide is rising! Water now covers the " + newWaterCols + " rightmost columns.");
        else
            notify("The tide is receding! Water now covers the " + newWaterCols + " rightmost columns.");
        currentWaterCols = newWaterCols;

        int cols = arena.getCols();
        for (Tile[] row : arena.getTiles()) {
            for (Tile tile : row) {
                if (!(tile instanceof LowShoreTile shore)) continue;

                boolean shouldBeFlooded = shore.getCol() >= cols - newWaterCols;
                if (shouldBeFlooded == shore.isFlooded()) continue;

                shore.setFlooded(shouldBeFlooded);
                if (shouldBeFlooded) drownLandPlants(shore, arena);
            }
        }
    }

    private void drownLandPlants(LowShoreTile shore, Arena arena) {
        List<Plant> plantsOnTile = shore.getPlants();
        if (plantsOnTile.isEmpty()) return;

        if (plantsOnTile.get(0).getTags().contains(PlantTag.WATER)) return;

        for (Plant plant : new ArrayList<>(plantsOnTile)) {
            notify(plant.getName() + " at row " + shore.getRow()
                    + ", col " + shore.getCol() + " was swallowed by the water!");
            plant.takeDamage(plant.getCurrentHp());
            plantsOnTile.remove(plant);
        }
    }
}
