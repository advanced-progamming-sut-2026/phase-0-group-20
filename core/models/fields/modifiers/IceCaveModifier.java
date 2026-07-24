package models.fields.modifiers;

import models.InGameEntityGenerator;
import models.Settings;
import models.entities.plants.Plant;
import models.entities.zombies.Wave;
import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.effect.FreezeEffect;
import models.enums.plants.PlantTag;
import models.fields.obstacle.IceBlock;
import models.fields.obstacle.IceHolder;
import models.fields.tiles.NormalTile;
import models.fields.tiles.SlipperyTile;
import models.fields.tiles.Tile;
import models.game.Arena;
import models.game.GameSession;

import java.util.*;

public class IceCaveModifier implements SeasonModifier {

    private static final double FREEZING_WIND_CHANCE = 0.4;
    private static final double SLIPPERY_STAGE_CHANCE = 0.5;
    private static final int MAX_FROSTBITE_LEVEL = 3;

    //for chance of slippery tile:
    private static final float DIFFICULTY_MULTIPLIER = 0.2f;
    private static final float BASE_SLIPPERY_SCALING = 0.4f;
    private static final float LEVEL_SCALING_FACTOR = 0.1f;

    private final Random rand = new Random();
    private final Map<Plant, Integer> frostbiteLevels = new HashMap<>();

    @Override
    public void onCurrentLevelStart() {
        Arena arena = GameSession.getInstance().getArena();

        setupSlipperyTiles(arena);
        setupInitialIceBlocks(arena);

    }

    @Override
    public void onWaveStart(Wave wave) {
        Arena arena = GameSession.getInstance().getArena();

        double currentWindChance = Math.min(0.8, FREEZING_WIND_CHANCE + 0.05 * getCurrentLevelNumber());

        if (rand.nextDouble() < currentWindChance)
            blowFreezingWind(arena);
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

        frostbiteLevels.keySet().removeIf(plant -> plant.getCurrentHp() <= 0);
    }

    private void setupSlipperyTiles(Arena arena) {
        float chance = (float) (getCurrentLevelNumber() * LEVEL_SCALING_FACTOR *
                (BASE_SLIPPERY_SCALING + DIFFICULTY_MULTIPLIER * Settings.getInstance().getDifficulty()));
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
                notify("An ice floe sliding " + direction
                        + " covers row " + randomRow + ", col " + randomCol + ".");
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
        int rows = arena.getRows();
        int numberOfLanes = rand.nextInt(2) + 1 + getCurrentLevelNumber() / 2;

        List<Integer> frozenRow = new ArrayList<>();
        while (frozenRow.size() < numberOfLanes) {
            int lane = rand.nextInt(rows);
            if (!frozenRow.contains(lane)) frozenRow.add(lane);
        }

        for (int row : frozenRow) {
            notify("A freezing wind sweeps through lane " + row + "!");
            for (Plant plant : new ArrayList<>(arena.getActivePlants())) {
                Tile tile = plant.getPlacedTile();
                if (tile == null || tile.getRow() != row) continue;
                if (plant.getTags().contains(PlantTag.FIRE)) continue;

                applyFrostbite(plant, arena);
            }
        }
    }

    private void applyFrostbite(Plant plant, Arena arena) {
        int level = frostbiteLevels.merge(plant, 1, Integer::sum);

        if (level >= MAX_FROSTBITE_LEVEL) {
            frostbiteLevels.remove(plant);
            freezePlant(plant, arena);
        } else
            notify(plant.getName() + " is at frostbite level " + level + "/" + MAX_FROSTBITE_LEVEL + ".");

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

        if (tile instanceof IceHolder) {
            IceBlock iceBlock = new IceBlock(plant, row, col);
            ((IceHolder) tile).setIceBlock(iceBlock);
            session.getTimeManager().registerNewTicker(iceBlock);
            notify(plant.getName() + " is completely frozen inside an IceBlock at row " +
                    row + ", col " + col + "!");
        }
    }

    private void setupInitialIceBlocks(Arena arena) {
        int rows = arena.getRows();
        int cols = arena.getCols();
        int numberOfIceBlocks = rand.nextInt(3) + getCurrentLevelNumber();

        int placed = 0;
        GameSession session = GameSession.getInstance();

        while (placed < numberOfIceBlocks) {
            int randomRow = rand.nextInt(rows);
            int randomCol = rand.nextInt(cols / 2) + 2;
            int rnd = rand.nextInt(2);
            randomCol = (rnd == 1) ? randomCol : arena.getCols() - randomCol;

            Tile tile = arena.getTile(randomRow, randomCol);

            if (tile instanceof IceHolder iceHolder && tile.getPlants().isEmpty() && !iceHolder.hasIceBlock()) {

                IceBlock iceBlock = null;

                if (rnd == 1) {
                    List<Plant> plants = GameSession.getInstance().getChosenPlants();
                    if (!plants.isEmpty()) {
                        Plant templatePlant = plants.get(rand.nextInt(plants.size()));
                        Plant freshPlant = InGameEntityGenerator.getPlantForGame(templatePlant, false);
                        iceBlock = new IceBlock(freshPlant, randomRow, randomCol);
                        notify("A pre-frozen Plant was placed at row " + randomRow + ", col " + randomCol);
                    }
                } else {
                    List<Zombie> zombies = GameSession.getInstance().getChosenZombies();
                    if (!zombies.isEmpty()) {
                        Zombie randomZombie = zombies.get(rand.nextInt(zombies.size()));
                        Zombie newZombie = InGameEntityGenerator.getZombieForGame(randomZombie.getType(), randomRow);
                        newZombie.setCol(randomCol);
                        iceBlock = new IceBlock(newZombie, randomRow, randomCol);
                        notify("A pre-frozen Zombie was placed at row " + randomRow + ", col " + randomCol);
                    }
                }

                if (iceBlock != null) {
                    iceHolder.setIceBlock(iceBlock);
                    session.getTimeManager().registerNewTicker(iceBlock);
                    placed++;
                }
            }
        }
    }
}
