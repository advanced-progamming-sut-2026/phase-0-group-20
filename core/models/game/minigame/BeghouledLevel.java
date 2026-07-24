package models.game.minigame;

import models.App;
import models.InGameEntityGenerator;
import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.entities.zombies.Zombie;
import models.enums.GameConstants;
import models.fields.tiles.Tile;
import models.game.Arena;
import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;
import models.game.adventure.levels.conditions.NormalLoseCondition;
import models.game.minigame.minigameCondition.BeghouledWinCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BeghouledLevel extends Level implements IMinigame {
    private final Random random = new Random();

    private final String[] basePlants = {
            "peashooter",
            "sunflower",
            "wall-nut",
            "snow pea",
            "repeater",
            "puff-shroom",
            "cabbage-pult",
            "melon-pult"};
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
        String message = "A new zombie " + newZombie.getName() + " has been spawned in" +
                "(" + (newZombie.getCol() + 1) + ", " + (newZombie.getRow() + 1) + ")!";

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

                String randomPlantName = basePlants[random.nextInt(basePlants.length)];
                Plant template = App.findPlantByName(randomPlantName);
                if (template != null) {
                    Plant newPlant = PlantFactory.create(template.getId());
                    tile.addPlant(newPlant);
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

}
