package io.java.pvz.models.fields.modifiers;

import io.java.pvz.models.App;
import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.obstacle.IceBlock;
import io.java.pvz.models.entities.obstacle.IceHolder;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Wave;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.behavior.effect.FreezeEffect;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.models.fields.tiles.NormalTile;
import io.java.pvz.models.fields.tiles.SlipperyTile;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.BossLevel;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class IceCaveModifier implements SeasonModifier {

    private static final double FREEZING_WIND_CHANCE = 0.4;
    private static final double SLIPPERY_STAGE_CHANCE = 0.5;

    //for chance of slippery tile:
    private static final float DIFFICULTY_MULTIPLIER = 0.2f;
    private static final float BASE_SLIPPERY_SCALING = 0.4f;
    private static final float LEVEL_SCALING_FACTOR = 0.1f;

    private final Random rand = new Random();

    private int windTimer = -1;
    private final List<Integer> upcomingWindRows = new ArrayList<>();

    @Override
    public void onCurrentLevelStart() {
        Arena arena = GameSession.getInstance().getArena();

        setupSlipperyTiles(arena);
        setupInitialIceBlocks(arena);

    }

    @Override
    public void onWaveStart(Wave wave) {
        double currentWindChance = Math.min(0.8, FREEZING_WIND_CHANCE + 0.05 * getCurrentLevelNumber());

        if (rand.nextDouble() < currentWindChance) {
            Arena arena = GameSession.getInstance().getArena();
            int rows = arena.getRows();
            int numberOfLanes = rand.nextInt(2) + 1 + getCurrentLevelNumber() / 2;

            upcomingWindRows.clear();
            while (upcomingWindRows.size() < numberOfLanes) {
                int lane = rand.nextInt(rows);
                if (!upcomingWindRows.contains(lane)) upcomingWindRows.add(lane);
            }

            windTimer = 5 * TimeManager.TICKS_PER_SECOND;

            for (int row : upcomingWindRows) {
                System.out.println("A freezing wind is approaching lane " + (row + 1) + "!");
            }
        }
    }

    @Override
    public void onZombieSpawn(Zombie zombie, Arena arena) {
        // zombies enter normally in the Frozen Caves
    }

    @Override
    public void updateEnvironment(int currentTick, Arena arena) {
        // in this season icy projectiles never freeze zombies

        for (Zombie zombie : arena.getActiveZombies()) {
            boolean frozen = zombie.getActiveEffects().stream()
                .anyMatch(zombieEffect -> zombieEffect instanceof FreezeEffect);
            if (frozen) zombie.removeFreezeEffect();
        }

        if (windTimer > 0) {
            windTimer--;
            if (windTimer == 0) {
                blowFreezingWind(arena);
            }
        }
    }

    private void setupSlipperyTiles(Arena arena) {
        float chance = (float) (getCurrentLevelNumber() * LEVEL_SCALING_FACTOR *
            (BASE_SLIPPERY_SCALING + DIFFICULTY_MULTIPLIER * App.getSettings().getDifficulty()));
        if (rand.nextDouble() >= SLIPPERY_STAGE_CHANCE + chance)
            return;

        int rows = arena.getRows();
        int cols = arena.getCols();
        int numberOfFTiles = rand.nextInt(2) + 2 + getCurrentLevelNumber();

        int placed = 0;

        long remainTiles = Arrays.stream(arena.getTiles())
            .flatMap(Arrays::stream)
            .filter(t -> t instanceof NormalTile && t.getPlants().isEmpty() && t.getCol() >= cols / 2)
            .count();

        while (placed < numberOfFTiles && remainTiles > 0) {
            int randomRow = rand.nextInt(rows);
            int randomCol = rand.nextInt(cols - (cols / 2)) + (cols / 2);

            Tile tile = arena.getTile(randomRow, randomCol);
            if (tile instanceof NormalTile && tile.getPlants().isEmpty()) {
                SlipperyTile.SlideDirection direction = pickDirection(randomRow, rows);
                arena.changeTile(randomRow, randomCol, new SlipperyTile(randomRow, randomCol, direction));
                placed++;
                remainTiles--;
            }
        }
    }

    private SlipperyTile.SlideDirection pickDirection(int row, int rows) {
        if (row == 0) return SlipperyTile.SlideDirection.DOWN;
        if (row == rows - 1) return SlipperyTile.SlideDirection.UP;
        return rand.nextBoolean() ? SlipperyTile.SlideDirection.UP : SlipperyTile.SlideDirection.DOWN;
    }

    private void blowFreezingWind(Arena arena) {
        for (int row : upcomingWindRows) {
            System.out.println("Freezing wind sweeps through lane " + (row + 1) + "!");

            GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
                new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                    .message("FREEZING_WIND")
                    .coordinate(row, 0)
                    .build());

            for (Plant plant : new ArrayList<>(arena.getActivePlants())) {
                Tile tile = plant.getPlacedTile();
                if (tile == null || tile.getRow() != row) continue;
                if (plant.getTags().contains(PlantTag.FIRE)) continue;

                plant.receiveIceHit();

                if (plant.isFrozen() && tile instanceof IceHolder iceHolder && !iceHolder.hasIceBlock()) {
                    freezePlant(plant, arena);
                }
            }
        }
        upcomingWindRows.clear();
    }

    private void freezePlant(Plant plant, Arena arena) {
        Tile tile = plant.getPlacedTile();
        if (tile == null) return;

        int row = tile.getRow();
        int col = tile.getCol();

        GameSession session = GameSession.getInstance();
        session.getTimeManager().unregisterTicker(plant);
        arena.getActivePlants().remove(plant);
        tile.getPlants().remove(plant);

        GameEventMessenger.getInstance().dispatch(GameEvent.SPAWN_EFFECT,
            new GameEventPayload.Builder(GameEvent.SPAWN_EFFECT)
                .message("REMOVE_ICE_OVERLAY")
                .plant(plant)
                .build());

        if (tile instanceof IceHolder) {
            IceBlock iceBlock = new IceBlock(plant, row, col);
            ((IceHolder) tile).setIceBlock(iceBlock);
            session.getTimeManager().registerNewTicker(iceBlock);
            arena.getActiveObstacles().add(iceBlock);
            System.out.println(plant.getName() + " is completely frozen inside an IceBlock at row " +
                (row + 1) + ", col " + (col + 1) + "!");
        }
    }

    private void setupInitialIceBlocks(Arena arena) {
        if (GameSession.getInstance().getCurrentMode() instanceof BossLevel) return;
        int rows = arena.getRows();
        int cols = arena.getCols();
        int numberOfIceBlocks = rand.nextInt(3) + getCurrentLevelNumber();

        int placed = 0;
        GameSession session = GameSession.getInstance();

        while (placed < numberOfIceBlocks) {
            int randomRow = rand.nextInt(rows);
            int randomCol = rand.nextInt(cols / 2) + 2;
            int rnd;

            if (placed == 0)
                rnd = 1;
            else if (placed == 1)
                rnd = 0;
            else
                rnd = rand.nextInt(2);

            randomCol = (rnd == 1) ? randomCol : arena.getCols() - randomCol;

            Tile tile = arena.getTile(randomRow, randomCol);

            if (tile instanceof IceHolder iceHolder && tile.getPlants().isEmpty() && !iceHolder.hasIceBlock()) {

                IceBlock iceBlock = null;

                if (rnd == 1) {
                    List<Plant> plants = GameSession.getInstance().getChosenPlants();
                    if (!plants.isEmpty()) {
                        Plant templatePlant = plants.get(rand.nextInt(plants.size()));
                        Plant freshPlant = InGameEntityGenerator.getPlantForGame(templatePlant, false);

                        freshPlant.setPlacedTile(tile);
                        io.java.pvz.models.entities.plants.effect.FreezeEffect freezeEffect =
                            new io.java.pvz.models.entities.plants.effect.FreezeEffect();
                        freshPlant.addEffect(freezeEffect);
                        freezeEffect.addStack(freshPlant);
                        freezeEffect.addStack(freshPlant);

                        iceBlock = new IceBlock(freshPlant, randomRow, randomCol);
                    }
                } else {
                    List<Zombie> zombies = GameSession.getInstance().getChosenZombies();
                    if (!zombies.isEmpty()) {
                        Zombie randomZombie = zombies.get(rand.nextInt(zombies.size()));
                        Zombie newZombie = InGameEntityGenerator.getZombieForGame(randomZombie.getType(), randomRow);
                        newZombie.setCol(randomCol);
                        iceBlock = new IceBlock(newZombie, randomRow, randomCol);
                    }
                }

                if (iceBlock != null) {
                    iceHolder.setIceBlock(iceBlock);
                    session.getTimeManager().registerNewTicker(iceBlock);
                    session.getArena().getActiveObstacles().add(iceBlock);
                    placed++;
                }
            }
        }
    }
}
