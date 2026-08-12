package io.java.pvz.models.game.minigame;

import io.java.pvz.models.App;
import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.SunType;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantFactory;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.fields.Brain;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.minigame.minigameCondition.IZombieLoseCondition;
import io.java.pvz.models.game.minigame.minigameCondition.IZombieWinCondition;
import io.java.pvz.models.timeManager.Ticker;

import java.util.List;
import java.util.Random;

public class IZombieLevel extends Level implements IMinigame {

    private final Random rand = new Random();
    private int redLineCol = 6;

    public IZombieLevel(String name, SeasonType seasonType, int waveCount, int levelNumber) {
        super(name, seasonType, waveCount, -1, levelNumber);
        this.addWinCondition(new IZombieWinCondition());
        this.addLoseCondition(new IZombieLoseCondition());
    }

    @Override
    public void onLevelStart(GameSession session) {

        session.getArena().removeLawnMowers();

        redLineCol = rand.nextInt(2) + 2 + levelNumber;

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
            int currentInterval = 120;

            @Override
            public void onTick(int currentTick) {
                if (sunZombie.isDead()) {
                    session.getTimeManager().unregisterTicker(this);
                    return;
                }

                ticksPassed++;
                if (ticksPassed >= currentInterval) {
                    session.getArena().addSun(new Sun(SunType.NORMAL_SUN, sunZombie.getCol(), sunZombie.getRow()));
                    ticksPassed = 0;
                    if (currentInterval > 40) currentInterval -= 12;
                }
            }

        });

        int numPlants = rand.nextInt(6) + 4 + levelNumber; // min: 4 different types
        List<Plant> availableTemplates = App.getActiveUser().getUnlockedPlants().stream()
                .filter(plant -> {
                    String plantName = plant.getName().toLowerCase();
                    return !plantName.contains("sun") && !plantName.equals("gold bloom") &&
                            !plantName.equals("grave buster") && !plantName.equals("hot potato") &&
                            !plantName.equals("lily pad") && !plantName.equals("tangle kelp") &&
                            !plantName.equals("sea-shroom") && !plantName.contains("mint");
                }).toList();
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

    @Override
    public MiniGameType getMiniGameType() {
        return MiniGameType.I_ZOMBIE;
    }

    @Override
    public boolean skipsPlantSelection() { return true; }

    public List<ZombieType> getZombiesForThisLevel() {
        return switch (levelNumber) {
            case 1 -> List.of(
                    ZombieType.NORMAL, ZombieType.CONE, ZombieType.BUCKET, ZombieType.IMP, ZombieType.ALL_STAR);
            case 2 -> List.of(ZombieType.NORMAL,
                    ZombieType.CONE, ZombieType.BUCKET, ZombieType.NEWSPAPER, ZombieType.DARK_ARMOR);
            case 3 -> List.of(ZombieType.NORMAL,
                    ZombieType.NEWSPAPER, ZombieType.BRICK, ZombieType.PROSPECTOR, ZombieType.GARGANTUAR);
            default -> List.of(ZombieType.CONE,
                    ZombieType.BUCKET, ZombieType.ALL_STAR, ZombieType.DARK_ARMOR, ZombieType.GARGANTUAR);
        };
    }
}
