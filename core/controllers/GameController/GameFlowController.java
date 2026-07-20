package controllers.GameController;

import models.InGameEntityGenerator;
import models.Result;
import models.entities.PlantFood;
import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
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



import java.util.ArrayList;
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
        return new Result(true, "Successfully advanced time for " + timeAmount + " ticks.");
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
        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.SUN_COLLECTED)
                .amount(sun.getType().getValue())
                .build();
        GameEventMessenger.getInstance().dispatch(GameEvent.SUN_COLLECTED, payload);

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
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();
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
        Plant plant = null;
        if (session.getCurrentMode() instanceof ConveyorBelt currentLevel) {
            List<Plant> belt = currentLevel.getBelt();
            if (belt.isEmpty()) {
                return new Result(false, "There is no plant in the belt rightNow.");
            }
            plant = belt.stream()
                    .filter(p -> p.getName().equalsIgnoreCase(plantName))
                    .findFirst()
                    .orElse(null);

        } else {
            plant = session.getChosenPlants().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(plantName))
                    .findFirst()
                    .orElse(null);
            if (plant != null) {
                if (session.getCurrentSun() < plant.getCost()) {
                    return new Result(false, "Not enough sun to plant " + plant.getName() + "!");
                }
                Integer cd = session.getPlantsCooldown().get(plant);
                if (cd != null && cd > 0) {
                    return new Result(false, plant.getName() + " is still recharging!");
                }
            }
        }
        if (plant == null) {
            return new Result(false, (session.getCurrentMode() instanceof ConveyorBelt)
                    ? "There is no such plant named " + plantName + "in the belt"
                    : "There no such plant named " + plantName);
        }
        Tile desiredTile = arena.getTile(spawnX - 1, spawnY - 1);
        if (desiredTile == null) {
            return new Result(false, "Az khat zadi biroon ke!!");
        }

        if (!desiredTile.isPlantable(plant)) {
            return new Result(false, "You can not plant this plant here");
        }

        Plant newPlant = InGameEntityGenerator.getPlantForGame(plant, plant.isBoosted());
        desiredTile.addPlant(newPlant);
        GameSession.getInstance().setPlantCooldown(newPlant);
        GameSession.getInstance().getArena().addPlant(newPlant);
        GameSession.getInstance().useSun(newPlant.getCost());
        GameSession.getInstance().getTimeManager().registerNewTicker(newPlant);
        GameEventPayload payload = new GameEventPayload.Builder(GameEvent.PLANT_PLACED)
                .plant(newPlant)
                .arena(arena)
                .coordinate(newPlant.getPlacedTile().getRow(), newPlant.getPlacedTile().getCol())
                .build();
        GameEventMessenger.getInstance().dispatch(GameEvent.PLANT_PLACED, payload);
        GameSession.getInstance().getTimeManager().registerNewTicker(newPlant);
        return new Result(true, "You plant a plant in " + spawnX + "," + spawnY + " with the name of " + newPlant.getName() + ".");
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
        for (Plant p : desiredTile.getPlants()) {
            GameSession.getInstance().getTimeManager().unregisterTicker(p);
            arena.getActivePlants().remove(p);
        }

        desiredTile.setPlants(Collections.emptyList()); // hal kardi parham?
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
            plant.useFood();
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
        StringBuilder mapOutput = new StringBuilder();

        mapOutput.append(buildHeader(arena, session));
        mapOutput.append(buildVisualGrid(arena));
        mapOutput.append(buildDetailedEntities(arena));

        return new Result(true, mapOutput.toString().trim());
    }

    private String buildHeader(Arena arena, GameSession session) {
        int currentWave = (arena.getCurrentActiveWave() != null) ? arena.getCurrentActiveWave().getCurrentNumber() : 0;
        int sunAmount = session.getCurrentSun();
        int plantFoodsCount = (session.getPlantFoods() != null) ? session.getPlantFoods().size() : 0;

        StringBuilder header = new StringBuilder();
        header.append("====================================================\n");
        header.append(String.format(" Wave: %d | Sun: %d | Plant Food: %d\n", currentWave, sunAmount, plantFoodsCount));
        header.append("====================================================\n");

        return header.toString();
    }

    private String buildVisualGrid(Arena arena) {
        StringBuilder grid = new StringBuilder();
        grid.append("\n--- Board Visual ---\n");

        int rows = arena.getRows();
        int cols = arena.getCols();
        LawnMower[] lawnMowers = arena.getLawnMowers();

        for (int row = 0; row < rows; row++) {
            boolean isMowerAvailable = (lawnMowers != null && row < lawnMowers.length && lawnMowers[row] != null);
            grid.append(isMowerAvailable ? "[LM] | " : "[  ] | ");

            List<Zombie> rowZombies = arena.zombieInRow(row);
            if (rowZombies == null) rowZombies = new ArrayList<>();

            for (int col = 0; col < cols; col++) {
                grid.append(getTileSymbol(arena.getTile(row, col), rowZombies, col));
            }
            grid.append("\n");
        }

        return grid.toString();
    }

    private String getTileSymbol(Tile tile, List<Zombie> rowZombies, int col) {
        boolean hasPlant = (tile != null && tile.getPlants() != null && !tile.getPlants().isEmpty());
        boolean hasZombie = false;

        for (Zombie z : rowZombies) {
            if (!z.isDead() && (int) z.getCol() == col) {
                hasZombie = true;
                break;
            }
        }

        if (hasPlant && hasZombie) return "[!X!] ";
        if (hasPlant) return "[ P ] ";
        if (hasZombie) return "[ Z ] ";

        return "[   ] ";
    }

    private String buildDetailedEntities(Arena arena) {
        StringBuilder details = new StringBuilder();
        details.append("\n--- Active Entities ---\n");
        boolean hasActiveEntities = false;

        int rows = arena.getRows();
        int cols = arena.getCols();

        for (int row = 0; row < rows; row++) {
            List<Zombie> rowZombies = arena.zombieInRow(row);
            if (rowZombies == null) rowZombies = new ArrayList<>();

            for (int col = 0; col < cols; col++) {
                String tileDetails = getTileDetails(arena.getTile(row, col), rowZombies, row, col);
                if (!tileDetails.isEmpty()) {
                    hasActiveEntities = true;
                    details.append(tileDetails);
                }
            }
        }

        if (!hasActiveEntities) {
            details.append("The lawn is currently empty.\n");
        }

        return details.toString();
    }

    private String getTileDetails(Tile tile, List<Zombie> rowZombies, int row, int col) {
        StringBuilder sb = new StringBuilder();
        List<Zombie> zombiesInTile = new ArrayList<>();

        for (Zombie z : rowZombies) {
            if (!z.isDead() && (int) z.getCol() == col) {
                zombiesInTile.add(z);
            }
        }

        List<Plant> plantsInTile = (tile != null && tile.getPlants() != null) ? tile.getPlants() : new ArrayList<>();

        if (zombiesInTile.isEmpty() && plantsInTile.isEmpty()) {
            return "";
        }
        sb.append(String.format("Tile (%d, %d):\n", col, row));

        if (!plantsInTile.isEmpty()) {
            sb.append("  [Plants]  ");
            List<String> plantNames = new ArrayList<>();
            for (Plant p : plantsInTile) {
                plantNames.add(p.getName());
            }
            sb.append(String.join(", ", plantNames)).append("\n");
        }

        if (!zombiesInTile.isEmpty()) {
            sb.append("  [Zombies] ");
            List<String> zombieDetails = new ArrayList<>();
            for (Zombie z : zombiesInTile) {
                zombieDetails.add(String.format("%s (%d)", z.getName(), z.getCol()));
            }
            sb.append(String.join(", ", zombieDetails)).append("\n");
        }
        sb.append("\n");

        return sb.toString();
    }
    public Result showCurrentState() {
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();
        StringBuilder mapDisplay = new StringBuilder();

        int currentWave = (session.getArena().getCurrentActiveWave() != null) ?
                session.getArena().getCurrentActiveWave().getCurrentNumber() : 0;
        int sunAmount = session.getCurrentSun();
        int plantFoodsCount = (session.getPlantFoods() != null) ? session.getPlantFoods().size() : 0;

        mapDisplay.append("==============================\n");
        mapDisplay.append("Wave: ").append(currentWave).append(" | ");
        mapDisplay.append("Sun: ").append(sunAmount).append(" | ");
        mapDisplay.append("Plant Food: ").append(plantFoodsCount).append("\n");
        mapDisplay.append("==============================\n\n");

        int rows = arena.getRows();
        int cols = arena.getCols();

        mapDisplay.append("--- PLANTS ---\n");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = arena.getTile(r, c);
                String cell = " ";

                if (tile != null && tile.getPlants() != null && !tile.getPlants().isEmpty()) {
                    cell = String.valueOf(tile.getPlants().get(0).getName().charAt(0));
                }
                mapDisplay.append("[").append(cell).append("]");
            }
            mapDisplay.append("\n");
        }
        mapDisplay.append("\n");

        mapDisplay.append("--- ZOMBIES ---\n");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String cell = " ";

                if (session.getArena().zombieInRow(r) != null) {
                    for (Zombie z : session.getArena().zombieInRow(r)) {
                        if (!z.isDead() && (int) z.getCol() == c) {
                            cell = String.valueOf(z.getName().charAt(0));
                            break;
                        }
                    }
                }
                mapDisplay.append("[").append(cell).append("]");
            }
            mapDisplay.append("\n");
        }
        mapDisplay.append("\n");

        mapDisplay.append("--- ITEMS (Suns/Coins/Etc) ---\n");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String cell = " ";

                mapDisplay.append("[").append(cell).append("]");
            }
            mapDisplay.append("\n");
        }
        mapDisplay.append("\n");

        return new Result(true, mapDisplay.toString().trim());
    }

    public Result showPlantsStatus() {
        GameSession session = GameSession.getInstance();
        StringBuilder statusDisplay = new StringBuilder();
        GameMode currentMode = session.getCurrentMode();
        if (currentMode instanceof ConveyorBelt conveyor) {
            List<Plant> belt = conveyor.getBelt();
            if (belt.isEmpty()) {
                return new Result(true, "The conveyor belt is currently empty! Wait for the next drop.");
            }
            statusDisplay.append("=== CONVEYOR BELT ===\n");
            for (int i = 0; i < belt.size(); i++) {
                statusDisplay.append("[").append(i + 1).append("] ").append(belt.get(i).getName()).append("\n");
            }
            statusDisplay.deleteCharAt(statusDisplay.length() - 1);
            return new Result(true, statusDisplay.toString());
        }
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
        statusDisplay.deleteCharAt(statusDisplay.length() - 1);
        return new Result(true, statusDisplay.toString());
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
