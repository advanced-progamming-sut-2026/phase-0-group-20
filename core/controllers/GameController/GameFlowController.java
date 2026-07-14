package controllers.GameController;

import models.Result;
import models.entities.PlantFood;
import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.fields.LawnMower;
import models.fields.tiles.*;
import models.game.Arena;
import models.game.GameSession;
import models.timeManager.TimeManager;

import java.util.Collections;
import java.util.HashMap;
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

    public Result collectSun(String colStr, String rowStr) {
        Arena arena = GameSession.getInstance().getArena();
        int col, row;
        try {
            col = Integer.parseInt(colStr);
            row = Integer.parseInt(rowStr);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        Sun sun = arena.getSunInCoordinate(col, row);
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
        if (spawnX < 1 || spawnY < 1) {
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
        Tile desiredTile = arena.getTile(spawnX - 1, spawnY - 1);
        if (desiredTile == null) {
            return new Result(false, "Az khat zadi biroon ke!!");
        }

        if (!desiredTile.isPlantable(plant)) {
            return new Result(false, "You can not plant this plant here");
        }

        desiredTile.addPlant(plant);
        GameSession.getInstance().setPlantCooldown(plant);
        return new Result(true, "You plant a plant in " + spawnX + "," + spawnY + " with the name of" + plant.getName() + ".");
    }

    public Result cheatRemoveCooldown() {
        GameSession.getInstance().instantiateCooldowns(GameSession.getInstance().getChosenPlants());

        return new Result(true, "Cheat Activated. All cooldowns have been removed.");
    }

    public Result pluckPlant(String x, String y) {
        int posX, posY;
        Arena arena = GameSession.getInstance().getArena();
        try {
            posX = Integer.parseInt(x);
            posY = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        if (posX < 1 || posY < 1) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        Tile desiredTile = arena.getTile(posX - 1, posY - 1);
        if (desiredTile == null) {
            return new Result(false, "Az khat zadi biroon ke!!");
        }
        if (desiredTile.getPlants().isEmpty()) {
            return new Result(false, "There is nothing to pluck my friend");
        }
        arena.getActivePlants().removeIf(p -> p.getPlacedTile().equals(desiredTile));// hal kardi parham?
        desiredTile.setPlants(Collections.emptyList());
        return new Result(true, "You successfully plucked all the plants in the tile");
    }

    public Result feedPlant(String x, String y) {
        int posX, posY;
        Arena arena = GameSession.getInstance().getArena();
        try {
            posX = Integer.parseInt(x);
            posY = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        if (posX < 1 || posY < 1) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        Tile desiredTile = arena.getTile(posX - 1, posY - 1);
        if (desiredTile == null) {
            return new Result(false, "Az khat zadi biroon ke!!");
        }
        if (desiredTile.getPlants().isEmpty()) {
            return new Result(false, "There is no plant in this tile");
        }
        for (Plant plant : desiredTile.getPlants()) {
            // elyas hanooz nazadeh
        }

        return new Result(true, "You successfully feed all the plants in the tile");

    }

    public Result cheatAddPlantFood() {
        GameSession session = GameSession.getInstance();
        List<PlantFood> plantFoods = session.getPlantFoods();
        if (plantFoods.size() >= 3) {
            return new Result(false, "You already have the maximum amount of the food plants");
        } else {
            PlantFood pf = new PlantFood(0, 0);
            pf.collect();
            plantFoods.add(pf);
            return new Result(true, "You successfully added the food plant");
        }

    }

    public Result showMap() {
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();
        StringBuilder mapDisplay = new StringBuilder();

        int currentWave = (session.getWaveManager() != null) ? session.getWaveManager().getCurrentNumber() : 0;
        int sunAmount = session.getCurrentSun();
        int plantFoodsCount = (session.getPlantFoods() != null) ? session.getPlantFoods().size() : 0;

        mapDisplay.append("Wave: ").append(currentWave).append("\n");
        mapDisplay.append("Sun: ").append(sunAmount).append("\n");
        mapDisplay.append("Plant Food: ").append(plantFoodsCount).append("\n");

        LawnMower[] lawnMowers = arena.getLawnMowers();
        int rows = arena.getRows();
        int cols = arena.getCols();

        for (int i = 0; i < rows; i++) {
            boolean isMowerAvailable = (lawnMowers != null && i < lawnMowers.length && lawnMowers[i] != null);
            mapDisplay.append("LawnMower ").append(i).append(": ")
                    .append(isMowerAvailable ? "activated" : "not activated").append("\n");
        }

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile tile = arena.getTile(row, col);

                mapDisplay.append("Tile ").append(row).append(" / ").append(col).append(":\n");

                java.util.List<Zombie> zombiesInTile = new java.util.ArrayList<>();
                if (session.getArena().zombieInRow(row) != null) {
                    for (Zombie z : session.getArena().zombieInRow(row)) {
                        if (!z.isDead() && (int) z.getCol() == col) {
                            zombiesInTile.add(z);
                        }
                    }
                }

                mapDisplay.append("-zombies:\n");
                if (zombiesInTile.isEmpty()) {
                    mapDisplay.append("     -None\n");
                } else {
                    mapDisplay.append("     -");
                    for (int k = 0; k < zombiesInTile.size(); k++) {
                        Zombie z = zombiesInTile.get(k);
                        mapDisplay.append(z.getName()).append(":")
                                .append(z.getCol()).append(",")
                                .append(z.getRow());

                        if (k < zombiesInTile.size() - 1) {
                            mapDisplay.append(", ");
                        }
                    }
                    mapDisplay.append("\n");
                }

                mapDisplay.append("-plants:\n");
                if (tile == null || tile.getPlants() == null || tile.getPlants().isEmpty()) {
                    mapDisplay.append("    -None\n");
                } else {
                    mapDisplay.append("    -");
                    java.util.List<Plant> plantsInTile = tile.getPlants();
                    for (int k = 0; k < plantsInTile.size(); k++) {
                        mapDisplay.append(plantsInTile.get(k).getName());

                        if (k < plantsInTile.size() - 1) {
                            mapDisplay.append(", ");
                        }
                    }
                    mapDisplay.append("\n");
                }
            }
        }

        return new Result(true, mapDisplay.toString().trim());
    }

    public Result showPlantsStatus() {
        GameSession session = GameSession.getInstance();
        StringBuilder statusDisplay = new StringBuilder();

        List<Plant> chosenPlants = session.getChosenPlants();
        int currentSun = session.getCurrentSun();
        HashMap<Plant, Integer> cooldowns = session.getPlantsCooldown();

        for (Plant plant : chosenPlants) {
            int cost = plant.getCost();
            int cooldownTicks = (cooldowns != null && cooldowns.containsKey(plant)) ? cooldowns.get(plant) : 0;
            int cooldownSeconds = cooldownTicks / TimeManager.TICKS_PER_SECOND;

            statusDisplay.append(plant.getName()).append(":\n");
            statusDisplay.append("    - Sun Cost: ").append(cost).append("\n");

            if (currentSun >= cost && cooldownTicks <= 0) {
                statusDisplay.append("    - Plantable: Yes\n");
            } else {
                statusDisplay.append("    - Plantable: No\n");

                if (cooldownTicks > 0) {
                    statusDisplay.append("    - Time until ready: ").append(cooldownSeconds).append(" seconds\n");
                }

                if (currentSun < cost) {
                    statusDisplay.append("    - Shortage: ").append(cost - currentSun).append(" sun\n");
                }
            }
        }

        return new Result(true, statusDisplay.toString().trim());
    }

    public Result showTileStatus(String x, String y) {
        int row, col;
        try {
            row = Integer.parseInt(x);
            col = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();

        if (row < 0 || row >= arena.getRows() || col < 0 || col >= arena.getCols()) {
            return new Result(false, "Invalid coordinates");
        }

        StringBuilder statusDisplay = new StringBuilder();
        Tile tile = arena.getTile(row, col);

        statusDisplay.append("Tile ").append(row).append(" / ").append(col).append(" Status:\n");

        String tileShape = "Normal";
        if (tile instanceof WaterTile || tile instanceof LowShoreTile) {
            tileShape = "Water";
        } else if (tile instanceof GraveStoneTile || tile instanceof NecromanceTile) {
            tileShape = "Grave";
        }
//        else if (tile instanceof FrozenTile) {
//            tileShape = "Frozen";
//        }

        statusDisplay.append("- Type: ").append(tileShape).append("\n");

        statusDisplay.append("- Plants:\n");
        if (tile == null || tile.getPlants() == null || tile.getPlants().isEmpty()) {
            statusDisplay.append("    - None\n");
        } else {
            for (Plant plant : tile.getPlants()) {
                statusDisplay.append("    - Name: ").append(plant.getName()).append("\n");
                statusDisplay.append("      Health: ").append(plant.getCurrentHp()).append("\n");
            }
        }

        statusDisplay.append("- Zombies:\n");
        boolean zombieFound = false;
        if (session.getArena().zombieInRow(row) != null) {
            for (Zombie z : session.getArena().zombieInRow(row)) {
                if (!z.isDead() && z.getCol() == col) {
                    zombieFound = true;
                    statusDisplay.append("    - Name: ").append(z.getName()).append("\n");
                    statusDisplay.append("      Health: ").append(z.getHealth()).append("\n");
                    statusDisplay.append("      Speed: ").append(String.format("%.2f", z.getCurrentSpeed())).append("\n");
                    statusDisplay.append("      Damage: ").append(z.getEatDps()).append("\n");
                }
            }
        }

        if (!zombieFound) {
            statusDisplay.append("    - None\n");
        }

        return new Result(true, statusDisplay.toString().trim());
    }

}
