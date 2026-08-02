package com.Project.PVZ.models.game.minigame;

import com.Project.PVZ.models.App;
import com.Project.PVZ.models.InGameEntityGenerator;
import com.Project.PVZ.models.Position;
import com.Project.PVZ.models.entities.plants.Plant;
import com.Project.PVZ.models.entities.plants.PlantFactory;
import com.Project.PVZ.models.entities.zombies.Zombie;
import com.Project.PVZ.models.enums.GameConstants;
import com.Project.PVZ.models.fields.tiles.Tile;
import com.Project.PVZ.models.game.Arena;
import com.Project.PVZ.models.game.GameSession;
import com.Project.PVZ.models.game.adventure.SeasonType;
import com.Project.PVZ.models.game.adventure.levels.Level;
import com.Project.PVZ.models.game.adventure.levels.conditions.NormalLoseCondition;
import com.Project.PVZ.models.game.minigame.minigameCondition.BeghouledWinCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeghouledLevel extends Level implements IMinigame {
    private final Random random = new Random();
    private final BeghouledManager manager = new BeghouledManager();

    private final List<String> basePlants = new ArrayList<>(List.of(
            "peashooter", "sunflower", "wall-nut", "snow pea", "cabbage-pult"
    ));

    // we can add or remove plants from this list
    private final int targetMatches;
    private int successfulMatches = 0;
    private int tickCounter = 0;
    private int currentSpawnInterval = GameConstants.SPAWN_IN_WAVE_INTERVAL;

    protected BeghouledLevel(String name, SeasonType season, int waveCount, int baseWaveBudget, int levelNumber) {
        super(name, season, waveCount, baseWaveBudget, levelNumber);
        this.targetMatches = 20 + (levelNumber * 5);


        this.addWinCondition(new BeghouledWinCondition());
        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onLevelStart(GameSession session) {
        fillBoardRandomly(session);
        notify("Make " + targetMatches + " matches!");
    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {
        tickCounter++;
        manager.tickUpdate(session);

        if (tickCounter >= currentSpawnInterval) {
            spawnSingleZombie(session);
            tickCounter = 0;

            if (currentSpawnInterval > 15) currentSpawnInterval--;

        }
    }

    private void spawnSingleZombie(GameSession session) {
        List<Zombie> allowedZombies = session.getChosenZombies();
        if (allowedZombies == null || allowedZombies.isEmpty()) return;

        Zombie template = allowedZombies.get(random.nextInt(allowedZombies.size()));
        int lane = random.nextInt(session.getArena().getRows());

        Zombie newZombie = InGameEntityGenerator.getZombieForGame(template.getType(), lane);
        newZombie.setCol(session.getArena().getCols() - 1);

        session.getArena().addZombie(newZombie);
        session.getTimeManager().registerNewTicker(newZombie);
        String message = "A new zombie "+newZombie.getName()+" has been spawned in" +
                "("+(newZombie.getCol()+1)+", "+(newZombie.getRow()+1)+")!";
        GameSession.notify(message);

    }

    public void fillBoardRandomly(GameSession session) {
        int rows = session.getArena().getRows();
        int cols = session.getArena().getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 1; c++) {
                Tile tile = session.getArena().getTile(r, c);

                if (tile.isCrater()) continue;

                if (!tile.getPlants().isEmpty()) {
                    tile.getPlants().clear();
                }

                String randomPlantName;
                do {
                    randomPlantName = basePlants.get(random.nextInt(basePlants.size()));
                } while (formsMatch(session, r, c, randomPlantName));

                Plant template = App.findPlantByName(randomPlantName);
                if (template != null) {
                    Plant newPlant = PlantFactory.create(template.getId());
                    tile.addPlant(newPlant);
                    newPlant.setPosition(new Position(tile.getCol(), tile.getRow()));
                    newPlant.setPlacedTile(tile);
                    session.getArena().addPlant(newPlant);
                    session.getTimeManager().registerNewTicker(newPlant);
                }
            }
        }
    }

    public void addSuccessfulMatch() {
        this.successfulMatches++;
    }

    public int getSuccessfulMatches() {
        return this.successfulMatches;
    }

    public int getTargetMatches() {
        return this.targetMatches;
    }

    @Override
    public boolean skySunFalls() {
        return false;
    }

    @Override
    public boolean skipsPlantSelection() {
        return true;
    }

    public String upgradePlants(String fromPlantName) {
        GameSession session = GameSession.getInstance();
        UpgradeInfo upgradeInfo = getUpgradeInfo(fromPlantName.toLowerCase());

        if (upgradeInfo == null) {
            return "Upgrade not available for " + fromPlantName;
        }

        if (session.getCurrentSun() < upgradeInfo.cost()) {
            return "Not enough sun! You need " + upgradeInfo.cost() + " suns.";
        }

        Plant template = App.findPlantByName(upgradeInfo.toPlantName());
        if (template == null) {
            return "Error: Target plant template not found.";
        }

        int upgradedCount = replacePlantsOnBoard(session, fromPlantName, template);

        if (upgradedCount > 0) {
            session.useSun(upgradeInfo.cost());

            int index = basePlants.indexOf(fromPlantName.toLowerCase());
            if (index != -1) {
                basePlants.set(index, upgradeInfo.toPlantName());
            }
        }

        return "Successfully upgraded " + upgradedCount + " " +
                fromPlantName + "s to " + upgradeInfo.toPlantName() + "!";
    }

    private int replacePlantsOnBoard(GameSession session, String fromPlantName, Plant template) {
        int upgradedCount = 0;
        Arena arena = session.getArena();

        for (int r = 0; r < arena.getRows(); r++) {
            for (int c = 0; c < arena.getCols(); c++) {
                Tile tile = arena.getTile(r, c);

                List<Plant> plantsOnTile = new ArrayList<>(tile.getPlants());

                for (Plant currentPlant : plantsOnTile) {
                    if (currentPlant.getName().equalsIgnoreCase(fromPlantName)) {

                        tile.getPlants().remove(currentPlant);
                        arena.getActivePlants().remove(currentPlant);
                        session.getTimeManager().unregisterTicker(currentPlant);

                        Plant newPlant = PlantFactory.create(template.getId());
                        tile.addPlant(newPlant);
                        arena.addPlant(newPlant);
                        session.getTimeManager().registerNewTicker(newPlant);

                        upgradedCount++;
                    }
                }
            }
        }
        return upgradedCount;
    }

    private UpgradeInfo getUpgradeInfo(String fromPlantName) {
        return switch (fromPlantName) {
            case "peashooter" -> new UpgradeInfo("repeater", 500);
            case "repeater" -> new UpgradeInfo("mega gatling pea", 1500);
            case "wall-nut" -> new UpgradeInfo("tall-nut", 500);
            case "puff-shroom" -> new UpgradeInfo("fume-shroom", 250);
            case "cabbage-pult" -> new UpgradeInfo("melon-pult", 1000);
            case "melon-pult" -> new UpgradeInfo("winter melon", 750);
            default -> null;
        };
    }

    private record UpgradeInfo(String toPlantName, int cost) {
    }

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.BEGHOULED;
    }

    public List<String> getBasePlants() {
        return basePlants;
    }

    private boolean formsMatch(GameSession session, int r, int c, String plantName) {
        Arena arena = session.getArena();

        if (c >= 2) {
            Plant p1 = getPlantAt(arena, r, c - 1);
            Plant p2 = getPlantAt(arena, r, c - 2);
            if (p1 != null && p2 != null &&
                    p1.getName().equalsIgnoreCase(plantName) && p2.getName().equalsIgnoreCase(plantName)) {
                return true;
            }
        }

        if (r >= 2) {
            Plant p1 = getPlantAt(arena, r - 1, c);
            Plant p2 = getPlantAt(arena, r - 2, c);
            if (p1 != null && p2 != null &&
                    p1.getName().equalsIgnoreCase(plantName) && p2.getName().equalsIgnoreCase(plantName)) {
                return true;
            }
        }

        return false;
    }

    private Plant getPlantAt(Arena arena, int r, int c) {
        Tile tile = arena.getTile(r, c);
        if (tile == null || tile.getPlants().isEmpty()) return null;
        return tile.getPlants().get(0);
    }

    public BeghouledManager getManager() {
        return manager;
    }

}
