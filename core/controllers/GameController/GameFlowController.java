package controllers.GameController;

import controllers.NavigationController;
import models.App;
import models.InGameEntityGenerator;
import models.Result;
import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieType;
import models.fields.LawnMower;
import models.fields.tiles.*;
import models.game.Arena;
import models.game.GameMode;
import models.game.GameSession;
import models.game.adventure.levels.speciallevels.ConveyorBelt;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;
import models.timeManager.TimeManager;
import models.users.User;

import java.util.ArrayList;
import java.util.HashMap;
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

    public Result advanceTime(String amount) {
        Integer timeAmount = parsePositiveInt(amount);
        if (timeAmount == null) {
            return new Result(false, "Invalid amount given. (Integer above ZERO)");
        }
        GameSession.getInstance().update(timeAmount);

        if (GameSession.getInstance().isGameOver()) {
            NavigationController.exitMenu();
            return new Result(true, "Returned to " + App.getActiveMenu().getName());
        }

        return new Result(true, "Successfully advanced time for " + timeAmount + " ticks.");
    }

    public Result showPlantFoodAmount(){
        int amount = App.getActiveUser().getPlantFoodCount();
        return new Result (true, "You currently have " + amount + " plants food left.");
    }

    public Result collectSun(String colStr, String rowStr) {
        Integer userCol = parsePositiveInt(colStr);
        Integer userRow = parsePositiveInt(rowStr);

        if (userCol == null || userRow == null) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }

        Arena arena = GameSession.getInstance().getArena();
        Sun sun = arena.getSunInCoordinate(userCol - 1, userRow - 1);
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

    public Result cheatAddSun(String amount) {
        Integer sunAmount = parsePositiveInt(amount);
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
            zombie.takeDamage(10000);
            GameEventPayload payload = new GameEventPayload.Builder(GameEvent.ZOMBIE_KILLED)
                    .zombie(zombie)
                    .coordinate(zombie.getRow(), zombie.getCol())
                    .arena(arena)
                    .seasonType(GameSession.getInstance().getCurrentChapter().getSeasonType())
                    .build();
            GameEventMessenger.getInstance().dispatch(GameEvent.ZOMBIE_KILLED, payload);
        }

        return new Result(true, "Nuked the whole arena!! Dast Khosh Donald.J.Trump.");
    }

    public Result showSunAmount() {
        int sunAmount = GameSession.getInstance().getCurrentSun();
        return new Result(true, "You currently have " + sunAmount + " suns in your pocket.");
    }

    public Result plantPlant(String plantName, String x, String y) {
        Integer spawnX = parsePositiveInt(x);
        Integer spawnY = parsePositiveInt(y);

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

        if (!desiredTile.isPlantable(plant)) {
            return new Result(false, "You can not plant this plant here");
        }

        Plant newPlant = InGameEntityGenerator.getPlantForGame(plant, plant.isBoosted());
        for(int i = 1 ; i<getPlantLevel(plant); i++) {
            newPlant.upgrade();
        }
        desiredTile.addPlant(newPlant);
        session.setPlantCooldown(newPlant);
        arena.addPlant(newPlant);
        session.useSun(newPlant.getCost());
        session.getTimeManager().registerNewTicker(newPlant);

        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.PLANT_PLACED)
                .plant(newPlant)
                .arena(arena)
                .coordinate(newPlant.getPlacedTile().getRow(), newPlant.getPlacedTile().getCol())
                .build();
        GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_PLACED, payload);
        session.getTimeManager().registerNewTicker(newPlant);

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
        if (!(session.getCurrentMode() instanceof ConveyorBelt)) {
            if (session.getCurrentSun() < plant.getCost()) {
                return new Result(false, "Not enough sun to plant " + plant.getName() + "!");
            }
            Integer cd = session.getPlantsCooldown().get(plant);
            if (cd != null && cd > 0) {
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
        user.addPlantFoodCount(-1);

        for (Plant plant : desiredTile.getPlants()) {
            if (plant.getPlantFoodStrategy() != null && !plant.getPlantFoodStrategy().isEmpty()) {
                plant.useFood();
            }
        }

        return new Result(true, "You successfully feed all the plants in the tile");
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

    private int getPlantLevel (Plant plant){
        User user = App.getActiveUser();
        return user.getUnlockedPlants().stream()
                .filter(p->p.getName().equals(plant.getName()))
                .findFirst().get().getLevel();
    }

}