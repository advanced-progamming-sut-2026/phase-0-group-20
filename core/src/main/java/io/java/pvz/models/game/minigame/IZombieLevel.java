package io.java.pvz.models.game.minigame;

import io.java.pvz.controllers.GameController.MatchController;
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
import io.java.pvz.models.game.RedLineCapable;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.minigame.minigameCondition.IZombieLoseCondition;
import io.java.pvz.models.game.minigame.minigameCondition.IZombieTimeLimitLoseCondition;
import io.java.pvz.models.game.minigame.minigameCondition.IZombieWinCondition;
import io.java.pvz.models.timeManager.Ticker;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IZombieLevel extends Level implements IMinigame, RedLineCapable {
    private static final int TIME_LIMIT = 120;
    private final Random rand = new Random();
    private int redLineCol = 6;
    private IZombieTimeLimitLoseCondition timeLimitCondition;

    private final List<Plant> belt = new ArrayList<>();
    private static final int BELT_MAX_CAPACITY = 10;
    private static final int BELT_SPAWN_INTERVAL_TICKS = 7 * TimeManager.TICKS_PER_SECOND;
    private int beltTicksPassed = 0;
    private List<Plant> beltPlantTemplates;

    public IZombieLevel(String name, SeasonType seasonType, int waveCount, int levelNumber) {
        super(name, seasonType, waveCount, -1, levelNumber);
        this.addWinCondition(new IZombieWinCondition());
        this.addLoseCondition(new IZombieLoseCondition());
        this.timeLimitCondition = new IZombieTimeLimitLoseCondition();
        this.addLoseCondition(this.timeLimitCondition);
    }

    public void setSurvivalTimeLimitSeconds(int seconds) {
        this.loseConditions.remove(this.timeLimitCondition);
        this.timeLimitCondition = new IZombieTimeLimitLoseCondition(seconds);
        this.addLoseCondition(this.timeLimitCondition);
    }

    public int getSurvivalTimeLimitTicks() {
        return timeLimitCondition.getTimeLimitTicks();
    }

    @Override
    public void onLevelStart(GameSession session) {
        belt.clear();
        beltTicksPassed = 0;

        session.getArena().removeLawnMowers();

        redLineCol = rand.nextInt(2) + 2 + levelNumber;

        boolean isMultiplayer = MatchController.getInstance().isOnlineMatch() ||
            MatchController.getInstance().isCouchPlay();

        for (int row = 0; row < session.getArena().getRows(); row++) {
            Brain brain = new Brain(row);
            session.getArena().setBrainInRow(row, brain);

            if (isMultiplayer) {
                spawnOnlySunZombie(session, row, session.getArena().getCols());
            } else {
                spawnPrePlacedPlants(session, row, redLineCol);
            }
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
            int currentInterval = 12 * TimeManager.TICKS_PER_SECOND; // 12 seconds

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
                    if (currentInterval > 4 * TimeManager.TICKS_PER_SECOND)
                        currentInterval -= TimeManager.TICKS_PER_SECOND;
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
        boolean isMultiplayer = MatchController.getInstance().isOnlineMatch() ||
            MatchController.getInstance().isCouchPlay();

        if (!isMultiplayer) return;

        beltTicksPassed++;
        if (beltTicksPassed < BELT_SPAWN_INTERVAL_TICKS) return;
        beltTicksPassed = 0;

        if (belt.size() >= BELT_MAX_CAPACITY) return;

        Plant newPlant = generateRandomBeltPlant();
        if (newPlant != null) {
            belt.add(newPlant);
        }
    }

    private Plant generateRandomBeltPlant() {
        if (beltPlantTemplates == null) {
            beltPlantTemplates = App.getActiveUser().getUnlockedPlants().stream()
                .filter(plant -> {
                    String plantName = plant.getName().toLowerCase();
                    return !plantName.contains("sun") && !plantName.equals("gold bloom") &&
                        !plantName.equals("grave buster") && !plantName.equals("hot potato") &&
                        !plantName.equals("lily pad") && !plantName.equals("tangle kelp") &&
                        !plantName.equals("sea-shroom") && !plantName.contains("mint");
                }).toList();
        }

        if (beltPlantTemplates.isEmpty()) return null;

        Plant template = beltPlantTemplates.get(rand.nextInt(beltPlantTemplates.size()));
        return PlantFactory.create(template.getId());
    }

    public boolean isValidZombiePlacement(int col) {
        return col >= redLineCol;
    }

    @Override
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
    public boolean skipsPlantSelection() {
        return true;
    }

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

    private void spawnOnlySunZombie(GameSession session, int row, int cols) {
        Zombie sunZombie = InGameEntityGenerator.getZombieForGame(ZombieType.BUCKET, row);
        sunZombie.setCol(cols - 1);
        sunZombie.setBaseSpeed(0);
        session.getArena().addZombie(sunZombie);

        session.getTimeManager().registerNewTicker(new Ticker() {
            int ticksPassed = 0;
            int currentInterval = 12 * TimeManager.TICKS_PER_SECOND;

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
                    if (currentInterval > 4 * TimeManager.TICKS_PER_SECOND)
                        currentInterval -= TimeManager.TICKS_PER_SECOND;
                }
            }
        });
    }

    @Override
    public String toString() {
        return "Don't Let Plants Destroy Your Zombies-Destroy All Of The Plants-" +
            "Win The Game in Under "+TIME_LIMIT+" Seconds";
    }

    public boolean isValidPlantPlacement(int col) {
        return col < redLineCol;
    }

    public List<Plant> getBelt() {
        return belt;
    }
}
