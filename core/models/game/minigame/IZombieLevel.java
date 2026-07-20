package models.game.minigame;

import models.App;
import models.InGameEntityGenerator;
import models.entities.Sun;
import models.entities.SunType;
import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieType;
import models.fields.Brain;
import models.game.GameSession;
import models.game.adventure.SeasonType;
import models.game.adventure.levels.Level;
import models.game.minigame.minigameCondition.IZombieLoseCondition;
import models.game.minigame.minigameCondition.IZombieWinCondition;
import models.timeManager.Ticker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class IZombieLevel extends Level {

    private final Random rand = new Random();
    private int redLineCol = 6;

    public IZombieLevel(String name, int levelNumber) {
        super(name, SeasonType.DARK_AGES, 1, -1, levelNumber);
        this.addWinCondition(new IZombieWinCondition());
        this.addLoseCondition(new IZombieLoseCondition());
    }

    @Override
    public void onStart(GameSession session) {
        System.out.println("iZombie Level " + levelNumber + " Started!");

        session.getArena().removeLawnMowers();

        redLineCol = rand.nextInt(4) + levelNumber;

        for (int row = 0; row < session.getArena().getRows(); row++) {
            Brain brain = new Brain(row);
            session.getArena().setBrainInRow(row, brain);
            spawnPrePlacedPlants(session, row, redLineCol);
        }

    }

    private void spawnPrePlacedPlants(GameSession session, int row, int redLineCol) {
        int cols = session.getArena().getCols();


        Zombie sunZombie = InGameEntityGenerator.getZombieForGame(ZombieType.BUCKET, row);
        sunZombie.setCol(cols - 1);
        sunZombie.setBaseSpeed(0);
        session.getArena().addZombie(sunZombie);

        session.getTimeManager().registerNewTicker(new Ticker() {
            int ticksPassed = 0;
            int currentInterval = 1000;

            @Override
            public void onTick(int currentTick) {
                if (sunZombie.isDead()) {
                    session.getTimeManager().unregisterTicker(sunZombie);
                    return;
                }

                ticksPassed++;
                if (ticksPassed >= currentInterval) {
                    session.getArena().addSun(new Sun(SunType.NORMAL_SUN, sunZombie.getCol(), sunZombie.getRow(), currentTick));
                    ticksPassed = 0;

                    if (currentInterval > 200) currentInterval -= 100;

                }
            }

        });

        int numPlants = rand.nextInt(6) + 3 + levelNumber; // min: 3 different types
        List<Plant> availableTemplates = new ArrayList<>(App.getAllPlants());
        Collections.shuffle(availableTemplates);
        List<Plant> selectedTemplates = availableTemplates.subList(0, Math.min(numPlants, availableTemplates.size()));


        for (int i = 0; i < redLineCol; i++) {
            Plant template = selectedTemplates.get(rand.nextInt(selectedTemplates.size()));
            Plant newPlant = PlantFactory.create(template.getId());

            session.getArena().addPlant(newPlant);
            session.getArena().getTile(row, i).addPlant(newPlant);
            session.getTimeManager().registerNewTicker(newPlant);

        }
    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {
        // we don't have spawn wave
    }

    public boolean isValidZombiePlacement(int col) {
        return col >= redLineCol;
    }

    public int getRedLineCol() {
        return redLineCol;
    }

    @Override
    public int getInitialSun() {
        return 150;
    }

    @Override
    public boolean skySunFalls() {
        return false;
    }
}