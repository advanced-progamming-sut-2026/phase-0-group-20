package models.game.minigame;

import models.App;
import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.fields.tiles.Tile;
import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;
import models.game.adventure.levels.conditions.NormalLoseCondition;
import models.game.minigame.minigameCondition.BeghouledWinCondition;

import java.util.Random;

public class BeghouledLevel extends Level {
    private final Random random = new Random();

    private final String[] basePlants = {"peashooter", "sunflower", "wall-nut", "snow pea", "repeater", "puff-shroom", "cabbage-pult", "melon-pult"};
    // we can add or remove plants from this list

    private int successfulMatches = 0;
    private final int targetMatches;

    protected BeghouledLevel(String name, SeasonType season, int waveCount, int baseWaveDifficulty, int levelNumber) {
        super(name, season, waveCount, baseWaveDifficulty, levelNumber);
        this.targetMatches = 20 + (levelNumber * 5);

        this.addWinCondition(new BeghouledWinCondition());
        this.addLoseCondition(new NormalLoseCondition());
    }

    @Override
    public void onStart(GameSession session) {
        fillBoardRandomly(session);
    }

    public void fillBoardRandomly(GameSession session) {
        int rows = session.getArena().getRows();
        int cols = session.getArena().getCols();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
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

    public String upgradePlants(String fromPlantName) {
        GameSession session = GameSession.getInstance();

        String toPlantName;
        int cost;

        switch (fromPlantName.toLowerCase()) {
            case "peashooter": toPlantName = "repeater"; cost = 500; break;
            case "repeater": toPlantName = "mega gatling pea"; cost = 1500; break;
            case "wall-nut": toPlantName = "tall-nut"; cost = 500; break;
            case "puff-shroom": toPlantName = "fume-shroom"; cost = 250; break;
            case "cabbage-pult": toPlantName = "melon-pult"; cost = 1000; break;
            case "melon-pult": toPlantName = "winter melon"; cost = 750; break;
            default: return "Upgrade not available for " + fromPlantName;
        }

        if (session.getCurrentSun() < cost) {
            return "Not enough sun! You need " + cost + " suns.";
        }

        session.useSun(cost);
        int upgradedCount = 0;

        int rows = session.getArena().getRows();
        int cols = session.getArena().getCols();

        Plant template = App.findPlantByName(toPlantName);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = session.getArena().getTile(r, c);
                if (!tile.getPlants().isEmpty()) {
                    Plant currentPlant = tile.getPlants().get(0);
                    if (currentPlant.getName().equalsIgnoreCase(fromPlantName)) {

                        tile.getPlants().clear();
                        Plant newPlant = PlantFactory.create(template.getId());
                        tile.addPlant(newPlant);
                        session.getArena().addPlant(newPlant);
                        session.getTimeManager().registerNewTicker(newPlant);

                        upgradedCount++;
                    }
                }
            }
        }

        return "Successfully upgraded " + upgradedCount + " " + fromPlantName + "s to " + toPlantName + "!";
    }

}
