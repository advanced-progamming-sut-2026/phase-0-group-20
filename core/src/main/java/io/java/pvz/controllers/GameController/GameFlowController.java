package io.java.pvz.controllers.GameController;

import io.java.pvz.controllers.NavigationController;
import io.java.pvz.models.App;
import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.Result;
import io.java.pvz.models.Settings;
import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.IPlantStrategy;
import io.java.pvz.models.entities.plants.strategy.ImitateStrategy;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.entities.zombies.behavior.effect.FireEffect;
import io.java.pvz.models.enums.GameState;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.Chapter;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.BonusLevel;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.adventure.levels.speciallevels.ConveyorBelt;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.game.minigame.BowlingLevel;
import io.java.pvz.models.users.User;

import java.util.ArrayList;
import java.util.List;

public class GameFlowController {

    private Integer parsePositiveInt(String str) {
        try {
            int val = Integer.parseInt(str);
            if (val > 0) return val;
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public Result advanceTime(int timeAmount) {
        GameSession.getInstance().update(timeAmount);

        if (GameSession.getInstance().isGameOver()) {
            App.getActiveUser().setPlantFoodCount(0);
            App.getActiveUser().getUnlockedPlants().forEach(p -> p.setBoosted(false));
            if (GameSession.getInstance().getCurrentMode() != null)
                ((Level) GameSession.getInstance().getCurrentMode()).destroyLevelFields();
        }

        return new Result(true, "Successfully advanced time for " + timeAmount + " ticks.");
    }

    public void gameOver() {
        if (GameSession.getInstance().isGameOver()) {
            NavigationController.exitMenu();
            App.getActiveUser().setPlantFoodCount(0);
            App.getActiveUser().getUnlockedPlants().forEach(p -> p.setBoosted(false));
            if (GameSession.getInstance().getCurrentMode() != null)
                ((Level) GameSession.getInstance().getCurrentMode()).destroyLevelFields();
        }
    }

    public Result showPlantFoodAmount() {
        int amount = App.getActiveUser().getPlantFoodCount();
        return new Result(true, "You currently have " + amount + " plants food left.");
    }

    public Result collectSun(int realCol, int realRow) {


        Arena arena = GameSession.getInstance().getArena();
        Sun sun = arena.getSunInCoordinate(realCol, realRow);
        if (sun == null) {
            return new Result(false, "There is no sun in this coordinate.");
        }

        sun.collect();
        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.SUN_COLLECTED)
            .amount(sun.getType().getValue())
            .build();
        GameEventMessenger.getInstance().dispatch(GameEvent.SUN_COLLECTED, payload);

        return new Result(true, "You collected a " + sun.getType().getLabel().toLowerCase() + " sun.");
    }

    public Result collectSun(Sun sun) {
        if (sun == null) {
            return new Result(false, "There is no sun to collect.");
        }

        if (sun.isCollected()) {
            return new Result(false, "Sun is already collected.");
        }

        sun.collect();
        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.SUN_COLLECTED)
            .amount(sun.getType().getValue())
            .build();
        GameEventMessenger.getInstance().dispatch(GameEvent.SUN_COLLECTED, payload);

        return new Result(true, "You collected a " + sun.getType().getLabel().toLowerCase() + " sun.");
    }

    public Result cheatAddSun(String amount) {
        Integer sunAmount = parsePositiveInt(amount);
        if(!App.getSettings().isDebug()){
            return new Result(false, "Cheat is not Allowed");
        }
        if (sunAmount == null) {
            return new Result(false, "Invalid amount given. (Integer above ZERO)");
        }
        GameSession.getInstance().addSun(sunAmount);
        return new Result(true, "Cheat Activated. added " + sunAmount + " suns to you cheater!!!");
    }

    public Result releaseNuke() {
        Arena arena = GameSession.getInstance().getArena();
        List<Zombie> activeZombies = arena.getActiveZombies();

        for (Zombie zombie : activeZombies) {
            if (zombie.getCol() >= 0 && zombie.getCol() < arena.getCols()) {

                zombie.addEffect(new FireEffect(zombie, 100000));

                GameEventPayload payload = new GameEventPayload.Builder(GameEvent.ZOMBIE_KILLED)
                    .zombie(zombie)
                    .coordinate(zombie.getRow(), zombie.getCol())
                    .arena(arena)
                    .seasonType(GameSession.getInstance().getCurrentChapter().getSeasonType())
                    .build();
                GameEventMessenger.getInstance().dispatch(GameEvent.ZOMBIE_KILLED, payload);
            }
        }

        return new Result(true, "Nuked the whole arena!! Dast Khosh Donald.J.Trump.");
    }

    public Result showSunAmount() {
        int sunAmount = GameSession.getInstance().getCurrentSun();
        return new Result(true, "You currently have " + sunAmount + " suns in your pocket.");
    }

    public Result plantPlant(String plantName, String col, String row) {
        Integer spawnX = parsePositiveInt(col);
        Integer spawnY = parsePositiveInt(row);
        if (spawnX == null || spawnY == null) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }

        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();

        Plant plant = findPlantForPlacement(session, plantName);
        if (plant == null) {
            return new Result(false, (session.getCurrentMode() instanceof ConveyorBelt)
                ? "There is no such plant named " + plantName + "in the belt"
                : "There no such plant named " + plantName);
        }

        Result validationResult = validatePlantPlacement(session, plant);
        if (validationResult != null) return validationResult;

        Tile desiredTile = arena.getTile(spawnY - 1, spawnX - 1);
        if (desiredTile == null) {
            return new Result(false, "Az khat zadi biroon ke!!");
        }
        Plant existingPlant = desiredTile.getStackPlant();
        if (existingPlant != null &&
            existingPlant.getName().equals(plant.getName()) && existingPlant.getTags().contains(PlantTag.STACK)) {
            if (existingPlant.addStack()) {
                session.useSun(plant.getCost());
                session.setCooldownForPlant(plant);
                GameEventPayload payload = new GameEventPayload.Builder(GameEvent.PLANT_PLACED)
                    .plant(existingPlant)
                    .arena(arena)
                    .coordinate(existingPlant.getPlacedTile().getRow(), existingPlant.getPlacedTile()
                        .getCol()).build();
                GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_PLACED, payload);
                return new Result(true, "You stacked " + plant.getName() + " to level " +
                    existingPlant.getStackCount() + " in " + spawnX + "," + spawnY);
            } else return new Result(false, "This " + plant.getName() + " is already fully stacked (Max 5)!");
        }

        if (!desiredTile.isPlantable(plant)) return new Result(false, "You can not plant this plant here");
        Plant newPlant = InGameEntityGenerator.getPlantForGame(plant, plant.isBoosted());
        for (int i = 1; i < getPlantLevel(plant); i++) newPlant.upgrade();

        if (newPlant.getName().equalsIgnoreCase("Imitater")) {
            for (IPlantStrategy strategy : newPlant.getStrategies()) {
                if (strategy instanceof ImitateStrategy imitateStrategy) {
                    int targetId = session.getImitaterTargetId();

                    if (targetId != -1) {
                        imitateStrategy.setTargetPlantId(targetId);
                    } else {
                        return new Result(false, "You haven't selected a target for Imitater in your deck!");
                    }
                    break;
                }
            }
        }

        desiredTile.addPlant(newPlant);
        arena.addPlant(newPlant);
        if (!((Level) GameSession.getInstance().getCurrentMode()).skipsPlantSelection()) {
            session.useSun(newPlant.getCost());
        }
        session.getTimeManager().registerNewTicker(newPlant);
        newPlant.setPlacedTile(desiredTile);
        if (plant.isBoosted()) {
            newPlant.useFood();
        }
        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.PLANT_PLACED)
            .plant(newPlant)
            .arena(arena)
            .coordinate(newPlant.getPlacedTile().getRow(), newPlant.getPlacedTile().getCol())
            .build();
        GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_PLACED, payload);
        session.setCooldownForPlant(plant);
        if (session.getCurrentMode() instanceof ConveyorBelt conveyorBelt) {
            var belt = conveyorBelt.getBelt();
            for (int i = 0; i < belt.size(); i++) {
                if (belt.get(i).getName().equalsIgnoreCase(plant.getName())) {
                    belt.remove(i);
                    break;
                }
            }
        }
        return new Result(true, "You plant a plant in " + spawnX + "," + spawnY +
            " with the name of " + newPlant.getName() + ".");
    }

    private Plant findPlantForPlacement(GameSession session, String plantName) {
        if (session.getCurrentMode() instanceof ConveyorBelt currentLevel) {
            List<Plant> belt = currentLevel.getBelt();
            if (belt.isEmpty()) return null;
            return belt.stream().filter(p -> p.getName().equalsIgnoreCase(plantName)).findFirst().orElse(null);
        } else {
            return session.getChosenPlants().stream()
                .filter(p -> p.getName().equalsIgnoreCase(plantName))
                .findFirst()
                .orElse(null);
        }
    }

    private Result validatePlantPlacement(GameSession session, Plant plant) {
        boolean usesUnlimitedBelt = session.getCurrentMode() instanceof ConveyorBelt
            || session.getCurrentMode() instanceof io.java.pvz.models.game.minigame.IZombieLevel;

        if (!usesUnlimitedBelt) {
            if (session.getCurrentSun() < plant.getCost()) {
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message("Not enough sun to plant " + plant.getName() + "!")
                        .build());
                return new Result(false, "Not enough sun to plant " + plant.getName() + "!");
            }
            Float cd = session.getPlantsCooldown().get(plant);
            if (cd != null && cd > 0) {
                GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                    new GameEventPayload.Builder(GameEvent.NOTIFY)
                        .message(plant.getName() + " is still recharging!")
                        .build());
                return new Result(false, plant.getName() + " is still recharging!");
            }
        }
        return null;
    }

    public Result cheatRemoveCooldown() {
        GameSession.getInstance().instantiateCooldowns(GameSession.getInstance().getChosenPlants());
        return new Result(true, "Cheat Activated. All cooldowns have been removed.");
    }

    public Result pluckPlant(String x, String y) {
        Integer posX = parsePositiveInt(x);
        Integer posY = parsePositiveInt(y);

        if (posX == null || posY == null) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }

        Arena arena = GameSession.getInstance().getArena();
        Tile desiredTile = arena.getTile(posY - 1, posX - 1);
        if (desiredTile == null) {
            return new Result(false, "Az khat zadi biroon ke!!");
        }
        if (desiredTile.getPlants().isEmpty()) {
            return new Result(false, "There is nothing to pluck my friend");
        }

        for (Plant p : desiredTile.getPlants()) {
            GameSession.getInstance().getTimeManager().unregisterTicker(p);
            arena.getActivePlants().remove(p);
        }

        desiredTile.setPlants(new ArrayList<>());
        return new Result(true, "You successfully plucked all the plants in the tile");
    }

    public Result feedPlant(String x, String y) {
        Integer posX = parsePositiveInt(x);
        Integer posY = parsePositiveInt(y);

        if (posX == null || posY == null) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }

        Arena arena = GameSession.getInstance().getArena();
        Tile desiredTile = arena.getTile(posY - 1, posX - 1);
        if (desiredTile == null) {
            return new Result(false, "Az khat zadi biroon ke!!");
        }
        if (desiredTile.getPlants().isEmpty()) {
            return new Result(false, "There is no plant in this tile");
        }

        User user = App.getActiveUser();
        if (user.getPlantFoodCount() <= 0) {
            return new Result(false, "You don't have any Plant Food.");
        }

        for (Plant plant : desiredTile.getPlants())
            if (plant.isBoosted())
                return new Result(false, "this plant already using food");

        user.addPlantFoodCount(-1);

        for (Plant plant : desiredTile.getPlants()) {
            if (plant.getPlantFoodStrategy() != null && !plant.getPlantFoodStrategy().isEmpty()) {
                plant.useFood();
            }
        }

        return new Result(true, "You successfully feed all the plants in the tile");
    }

    public Result cheatSpawnZombie(String zombieTypeStr, String x, String y) {
        Integer spawnX = parsePositiveInt(x);
        Integer spawnY = parsePositiveInt(y);

        if (spawnX == null || spawnY == null)
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");

        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();

        if (spawnY - 1 >= arena.getRows() || spawnX - 1 >= arena.getCols())
            return new Result(false, "Az khat zadi biroon ke!!");

        ZombieType type = null;
        for (ZombieType zombieType : ZombieType.values()) {
            if (zombieType.name().replace("_", "")// look at names we don't need _
                .equalsIgnoreCase(zombieTypeStr.replace(" ", ""))) {
                type = zombieType;
                break;
            }
        }

        if (type == null)
            return new Result(false, "Invalid zombie type: " + zombieTypeStr);

        Zombie newZombie = InGameEntityGenerator.getZombieForGame(type, spawnY - 1);
        newZombie.setCol(spawnX - 1);
        arena.addZombie(newZombie);
        session.getTimeManager().registerNewTicker(newZombie);

        return new Result(true, "Cheat Activated. Spawned " + newZombie.getName() +
            " at (" + spawnX + ", " + spawnY + ").");
    }

    public Result cheatAddPlantFood() {
        User user = App.getActiveUser();
        int plantFoodCount = user.getPlantFoodCount();
        if (plantFoodCount >= 3) {
            return new Result(false, "You already have the maximum amount of the food plants");
        } else {
            user.addPlantFoodCount(1);
        }
        return new Result(true, "You successfully gained a food plant");
    }

    public Result restartLevel() {
        GameSession session = GameSession.getInstance();
        if (session == null || !(session.getCurrentMode() instanceof Level levelToRestart)) {
            return new Result(false, "No active level to restart!");
        }

        Chapter currentChapter = session.getCurrentChapter();

        GameSession.destroyInstance();

        if (levelToRestart instanceof BonusLevel bonusLevel) {
            GameSession.setPendingBonusLevel(bonusLevel);
        } else if (levelToRestart.getSeason() == SeasonType.MINI_GAME) {
            GameSession.setMinigameLevel(levelToRestart);
        } else {
            GameSession.setPendingLevel(levelToRestart);
            if (currentChapter != null) {
                GameSession.setPendingChapter(currentChapter);
            }
        }

        if (levelToRestart.skipsPlantSelection()) {
            if (levelToRestart instanceof BowlingLevel bowlingLevel) {
                if (levelToRestart.getSeason() == SeasonType.MINI_GAME) {
                    GameSession.startMiniGame(levelToRestart, bowlingLevel.getBelt());
                } else {
                    GameSession.startNewGame(bowlingLevel.getBelt());
                }
            }else if (levelToRestart instanceof ConveyorBelt conveyorBelt) {
                conveyorBelt.getBelt().clear();
                if (levelToRestart.getSeason() == SeasonType.MINI_GAME) {
                    GameSession.startMiniGame(levelToRestart, conveyorBelt.getBelt());
                } else {
                    GameSession.startNewGame(conveyorBelt.getBelt());
                }
            }
            App.setActiveMenu(Menu.GAME_FLOW_MENU);
        } else {
            App.setActiveMenu(Menu.PLANTSELLECTION_MENU);
        }

        return new Result(true, "Level restarted successfully.");
    }

    private int getPlantLevel(Plant plant) {
        User user = App.getActiveUser();
        return user.getUnlockedPlants().stream()
            .filter(p -> p.getName().equals(plant.getName()))
            .findFirst().get().getLevel();
    }



}
