package controllers.GameController;

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
import models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameMapController {
    private Integer parsePositiveInt(String str) {
        try {
            int val = Integer.parseInt(str);
            if (val > 0) return val;
        } catch (NumberFormatException ignored) {
        }
        return null;
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
        Integer userCol = parsePositiveInt(x);
        Integer userRow = parsePositiveInt(y);

        if (userCol == null || userRow == null) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }

        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();
        int internalCol = userCol - 1;
        int internalRow = userRow - 1;

        if (internalRow >= arena.getRows() || internalCol >= arena.getCols()) {
            return new Result(false, "Invalid coordinates");
        }

        Tile tile = arena.getTile(internalRow, internalCol);
        StringBuilder statusDisplay = new StringBuilder();
        statusDisplay.append("Tile ").append(userRow).append(" / ").append(userCol).append(" Status:\n");
        statusDisplay.append("- Type: ").append(determineTileShape(tile)).append("\n");

        appendTilePlantsStatus(statusDisplay, tile);
        appendTileZombiesStatus(statusDisplay, session, internalRow, internalCol);

        return new Result(true, statusDisplay.toString().trim());
    }

    private String determineTileShape(Tile tile) {
        if (tile instanceof WaterTile || tile instanceof LowShoreTile) {
            return "Water";
        } else if (tile instanceof GraveStoneTile || tile instanceof NecromanceTile) {
            return "Grave";
        }
        return "Normal";
    }

    private void appendTilePlantsStatus(StringBuilder sb, Tile tile) {
        sb.append("- Plants:\n");
        if (tile == null || tile.getPlants() == null || tile.getPlants().isEmpty()) {
            sb.append("    - None\n");
        } else {
            for (Plant plant : tile.getPlants()) {
                sb.append("    - Name: ").append(plant.getName()).append("\n");
                sb.append("      Health: ").append(plant.getCurrentHp()).append("\n");
                sb.append(" level:").append(plant.getLevel()).append("\n");
            }
        }
    }

    private void appendTileZombiesStatus(StringBuilder sb, GameSession session, int row, int col) {
        sb.append("- Zombies:\n");
        boolean zombieFound = false;

        if (session.getArena().zombieInRow(row) != null) {
            for (Zombie z : session.getArena().zombieInRow(row)) {
                if (!z.isDead() && (int) z.getCol() == col) {
                    zombieFound = true;
                    sb.append("    - Name: ").append(z.getName()).append("\n");
                    sb.append("      Health: ").append(z.getHealth()).append("\n");
                    sb.append("      Speed: ").append(String.format("%.2f", z.getCurrentSpeed())).append("\n");
                    sb.append("      Damage: ").append(z.getEatDps()).append("\n");
                }
            }
        }

        if (!zombieFound) {
            sb.append("    - None\n");
        }
    }

    public Result printMap() {
        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();
        StringBuilder sb = new StringBuilder();
        String horizontalBorder = "+----------".repeat(arena.getCols()) + "+\n";

        sb.append("\n=== PIXEL-PERFECT ARENA MAP ===\n");

        for (int r = 0; r < arena.getRows(); r++) {
            sb.append(horizontalBorder);
            sb.append(renderPixelRow(arena, r));

            LawnMower lm = arena.getLawnMowers()[r];
            if (lm != null && !lm.isActivate()) {
                sb.append(" [LM]");
            }
            sb.append("\n");
        }
        sb.append(horizontalBorder);
        sb.append("\nLegend: [+] Plant | [*] Zombie | [-] Projectile | [s] Sun | [O] Crater\n");
        sb.append("Tiles:  [N ] Normal | [W~] Water | [L/] LowShore |" +
                " [S#] Slippery | [G / NG] Graves | [PV/ZV/RV] Vases\n");

        return new Result(true, sb.toString());
    }

    private String renderPixelRow(Arena arena, int row) {
        char[] rowContent = new char[arena.getCols() * 10];

        fillBaseTileSymbols(arena, row, rowContent);
        overlayEntitiesOnRow(arena, row, rowContent);

        StringBuilder sb = new StringBuilder("|");
        for (int c = 0; c < arena.getCols(); c++) {
            sb.append(new String(rowContent, c * 10, 10)).append("|");
        }
        return sb.toString();
    }

    private void fillBaseTileSymbols(Arena arena, int row, char[] rowContent) {
        for (int c = 0; c < arena.getCols(); c++) {
            Tile tile = arena.getTile(row, c);
            if (tile != null) {
                String prefix = getTilePrefix(tile.getType(), tile);
                rowContent[c * 10] = prefix.charAt(0);
                rowContent[c * 10 + 1] = prefix.charAt(1);
                for (int i = 2; i < 10; i++) rowContent[c * 10 + i] = ' ';
                if (tile.isCrater()) rowContent[c * 10 + 2] = 'O';
            } else {
                for (int i = 0; i < 10; i++) rowContent[c * 10 + i] = ' ';
            }
        }
    }

    private String getTilePrefix(String tileType, Tile tile) {
        return switch (tileType) {
            case "WaterTile" -> "W~";
            case "LowShoreTile" -> "L/";
            case "SlipperyTile" -> {
                String arrow = (tile instanceof SlipperyTile s && s.getDirection()
                        == SlipperyTile.SlideDirection.UP) ? "^" : "v";
                yield "S" + arrow;
            }
            case "GraveStone" -> "G ";
            case "NecromancyTile" -> "NG";
            case "PlantVaseTile" -> "PV";
            case "ZombieVaseTile" -> "ZV";
            case "RandomVaseTile" -> "RV";
            case "VaseTile" -> "V ";
            default -> "N ";
        };
    }

    private void overlayEntitiesOnRow(Arena arena, int row, char[] rowContent) {
        for (Sun sun : arena.getActiveSuns()) {
            if (!sun.isCollected() && sun.getRow() == row) {
                int pos = sun.getCol() * 10 + 2;
                if (pos >= 0 && pos < rowContent.length) rowContent[pos] = 's';
            }
        }
        for (Plant p : arena.getActivePlants()) {
            if (p.getPlacedTile() != null && p.getPlacedTile().getRow() == row) {
                int pos = p.getPlacedTile().getCol() * 10 + 4;
                if (pos >= 0 && pos < rowContent.length) rowContent[pos] = '+';
            }
        }
        for (Zombie z : arena.getActiveZombies()) {
            if (!z.isDead() && z.getRow() == row) {
                int pos = (int) z.getX();
                if (pos >= 0 && pos < rowContent.length) rowContent[pos] = '*';
            }
        }
        for (Projectile p : arena.getActiveProjectiles()) {
            if (!p.isDestroyed() && p.getPosition().getRow() == row) {
                int pos = (int) p.getX();
                if (pos >= 0 && pos < rowContent.length) rowContent[pos] = '-';
            }
        }
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
