package controllers.GameController;

import models.App;
import models.InGameEntityGenerator;
import models.Result;
import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
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
import models.users.User;

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

        if (col < 1 || row < 1) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }

        Sun sun = arena.getSunInCoordinate(col - 1, row - 1);
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
        Tile desiredTile = arena.getTile(spawnY - 1, spawnX - 1);
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
        session.getTimeManager().registerNewTicker(newPlant);
//        session.setCooldownForPlant(plant);
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
            desiredTile.setPlants(Collections.emptyList());
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
            if (!plant.getPlantFoodStrategy().isEmpty() && plant.getPlantFoodStrategy() != null) {
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
        sb.append(String.format("Tile (%d, %d):\n", col + 1, row + 1));

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


        mapDisplay.append("--- PROJECTILES ---\n");
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                String cell = " ";

                if (arena.getActiveProjectiles() != null) {
                    for (Projectile p : arena.getActiveProjectiles()) {
                        if (!p.isDestroyed() && (int) p.getY() == r && (int) p.getX() == c) {
                            cell = "*";
                            break;
                        }
                    }
                }
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
        int userCol, userRow;
        try {
            userCol = Integer.parseInt(x);
            userRow = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }

        if (userCol < 1 || userRow < 1) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }

        int internalCol = userCol - 1;
        int internalRow = userRow - 1;

        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();

        if (internalRow >= arena.getRows() || internalCol >= arena.getCols()) {
            return new Result(false, "Invalid coordinates");
        }

        StringBuilder statusDisplay = new StringBuilder();
        Tile tile = arena.getTile(internalRow, internalCol);

        statusDisplay.append("Tile ").append(userRow).append(" / ").append(userCol).append(" Status:\n");

        String tileShape = "Normal";
        if (tile instanceof WaterTile || tile instanceof LowShoreTile) {
            tileShape = "Water";
        } else if (tile instanceof GraveStoneTile || tile instanceof NecromanceTile) {
            tileShape = "Grave";
        }
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

        if (session.getArena().zombieInRow(internalRow) != null) {
            for (Zombie z : session.getArena().zombieInRow(internalRow)) {
                if (!z.isDead() && (int) z.getCol() == internalCol) {
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


    public Result printMap() {
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();
        StringBuilder sb = new StringBuilder();

        String horizontalBorder = "+----------".repeat(arena.getCols()) + "+\n";

        sb.append("\n=== PIXEL-PERFECT ARENA MAP ===\n");

        for (int r = 0; r < arena.getRows(); r++) {
            sb.append(horizontalBorder);

            char[] rowContent = new char[arena.getCols() * 10];

            for (int c = 0; c < arena.getCols(); c++) {
                Tile tile = arena.getTile(r, c);

                char bg = ' ';
                String prefix = "N ";

                if (tile != null) {
                    switch (tile.getType()) {
                        case "WaterTile" -> prefix = "W~";
                        case "LowShoreTile" -> prefix = "L/";
                        case "SlipperyTile" -> {
                            String arrow = "";
                            if (tile instanceof SlipperyTile slipperyTile)
                                arrow += slipperyTile.getDirection() == SlipperyTile.SlideDirection.UP ? "^" : "v";
                            prefix = "S" + arrow;
                        }
                        case "GraveStone" -> prefix = "G ";
                        case "NecromancyTile" -> prefix = "NG";
                        case "PlantVaseTile" -> prefix = "PV";
                        case "ZombieVaseTile" -> prefix = "ZV";
                        case "RandomVaseTile" -> prefix = "RV";
                        case "VaseTile" -> prefix = "V ";
                        case "NormalTile" -> prefix = "N ";
                    }


                    rowContent[c * 10] = prefix.charAt(0);
                    rowContent[c * 10 + 1] = prefix.charAt(1);

                    for (int i = 2; i < 10; i++)
                        rowContent[c * 10 + i] = bg;


                    if (tile.isCrater())
                        rowContent[c * 10 + 2] = 'O';

                } else
                    for (int i = 0; i < 10; i++) rowContent[c * 10 + i] = ' ';

            }


            for (Sun sun : arena.getActiveSuns()) {
                if (!sun.isCollected() && sun.getRow() == r) {
                    int pos = sun.getCol() * 10 + 2;
                    if (pos >= 0 && pos < rowContent.length) rowContent[pos] = 's';
                }
            }

            for (Plant p : arena.getActivePlants()) {
                if (p.getPlacedTile() != null && p.getPlacedTile().getRow() == r) {
                    int pos = p.getPlacedTile().getCol() * 10 + 4;
                    if (pos >= 0 && pos < rowContent.length) rowContent[pos] = '+';
                }
            }

            for (Zombie z : arena.getActiveZombies()) {
                if (!z.isDead() && z.getRow() == r) {
                    int pos = (int) z.getX();
                    if (pos >= 0 && pos < rowContent.length) rowContent[pos] = '*';
                }
            }

            for (Projectile p : arena.getActiveProjectiles()) {
                if (!p.isDestroyed() && p.getPosition().getRow() == r) {
                    int pos = (int) p.getX();
                    if (pos >= 0 && pos < rowContent.length) rowContent[pos] = '-';
                }
            }

            sb.append("|");
            for (int c = 0; c < arena.getCols(); c++)
                sb.append(new String(rowContent, c * 10, 10)).append("|");


            LawnMower lm = arena.getLawnMowers()[r];
            if (lm != null && !lm.isActivate())
                sb.append(" [LM]");

            sb.append("\n");
        }
        sb.append(horizontalBorder);

        sb.append("\nLegend: [+] Plant | [*] Zombie | [-] Projectile | [s] Sun | [O] Crater\n");
        sb.append("Tiles:  [N ] Normal | [W~] Water | [L/] LowShore | [S#] Slippery | [G / NG] Graves | [PV/ZV/RV] Vases\n");
        return new

                Result(true, sb.toString());
    }


    public Result showZombieInfo() {
        if (GameSession.getInstance().getArena().getActiveZombies().isEmpty()) {
            return new Result(false, "There are no active zombies");
        }

        StringBuilder sb = new StringBuilder();

        for (Zombie z : GameSession.getInstance().getArena().getActiveZombies()) {
            sb.append(z.getInGameDetails()).append("\n\n");
        }

        return new Result(true, sb.toString().trim());
    }


}
