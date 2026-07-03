package controllers.GameController;

import models.Result;
import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.game.Arena;
import models.game.GameSession;

import java.util.List;

public class GameFlowController {

    public Result advanceTime(String amount) {
        int timeAmount = 0;
        try {
            timeAmount = Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid amount given. (Integer above ZERO)");
        }
        if (timeAmount <= 0) {
            return new Result(false, "Even Dr.Strange couldn't travel to the past.\nwho the are you?");
        }
        GameSession.getInstance().update(timeAmount);
        return new Result(true, "Successfully advanced time for " + timeAmount + " seconds.");
    }

    public Result collectSun(String x, String y) {
        Arena arena = GameSession.getInstance().getArena();
        int posX, posY;
        try {
            posX = Integer.parseInt(x);
            posY = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        Sun sun = arena.getSunInCoordinate(posX, posY);
        if (sun == null) {
            return new Result(false, "There is no sun in this coordinate.");
        }

        sun.collect();
        return new Result(true, "You collected a " + sun.getType().getLabel().toLowerCase() +
                "sun.");
    }

    public Result cheatAddSun(String amount) {
        int sunAmount;
        try {
            sunAmount = Integer.parseInt(amount);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid amount given. (Integer above ZERO)");
        }
        if (sunAmount <= 0) {
            return new Result(false, "Invalid amount given. (Integer above ZERO)");
        }
        GameSession.getInstance().addSun(sunAmount);

        return new Result(true, "Cheat Activated. added " + sunAmount + " suns to you cheater!!!");
    }

    public Result releaseNuke() {
        Arena arena = GameSession.getInstance().getArena();
        List<Zombie> activeZombies = arena.getActiveZombies();

        for (Zombie zombie : activeZombies) {
            zombie.takeDirectDamage(10000);
        }

        return new Result(true, "Nuked the whole arena!! Dast Khosh Donald.J.Trump.");
    }

    public Result showSunAmount() {
        int sunAmount = GameSession.getInstance().getCurrentSun();

        return new Result(true, "You currently have " + sunAmount + " suns in your pocket.");
    }

    public Result plantPlant(String plantName, String x, String y) {
        Arena arena = GameSession.getInstance().getArena();
        int spawnX, spawnY;
        try {
            spawnX = Integer.parseInt(x);
            spawnY = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        List<Plant> chosenPlants = GameSession.getInstance().getChosenPlants();
        Plant plant = chosenPlants.stream()
                .filter(p -> p.getName().equalsIgnoreCase(plantName))
                .findFirst()
                .orElse(null);
        if (plant == null) {
            return new Result(false, "There no such plant named " + plantName);
        }

        // We need to implement the fields first then add this part.
        return new Result(true, "You plant a plant in " + spawnX + "," + spawnY + " with the name of" + plant.getName() + ".");
    }

    public Result cheatRemoveCooldown() {
        GameSession.getInstance().instantiateCooldowns(GameSession.getInstance().getChosenPlants());

        return new Result(true, "Cheat Activated. All cooldowns have been removed.");
    }

    public Result pluckPlant(String x, String y) {
        return null;// this too.
    }

    public Result feedPlant(String x, String y) {
        return null;
    }

    public Result cheatAddPlantFood() {
        return null;
    }

    public Result showMap() {
        return null;
    }

    public Result showPlantsStatus() {
        return null;
    }

    public Result showTileStatus(String x, String y) {
        return null;
    }

}
