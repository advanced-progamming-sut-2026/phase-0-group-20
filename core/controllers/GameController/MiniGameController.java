package controllers.GameController;

import models.InGameEntityGenerator;
import models.Position;
import models.Result;
import models.entities.Sun;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieType;
import models.enums.PhysicalConstants;
import models.enums.plants.ProjectileType;
import models.fields.Brain;
import models.fields.LawnMower;
import models.fields.tiles.SlipperyTile;
import models.fields.tiles.Tile;
import models.fields.tiles.VaseInside;
import models.fields.tiles.VaseTile;
import models.game.Arena;
import models.game.GameSession;
import models.game.adventure.levels.Level;
import models.game.minigame.*;

import java.util.ArrayList;
import java.util.List;

public class MiniGameController {

    public Result changeMenu(String newMenu) {
        return null;
    }

    public Result handleRule(String newMenu) {
        return null;
    }

    public Result breakVase(int row, int col) {
        Tile tile = GameSession.getInstance().getArena().getTile(row, col);

        if (tile instanceof VaseTile vase) {
            VaseInside vaseInside = vase.breakVase();

            switch (vaseInside) {
                case SEED_PACKET -> {
                    return new Result(true, "You found a seed packet");
                }
                case ZOMBIE -> {
                    return new Result(true, "A zombie emerged");
                }
                default -> {
                    return new Result(false, "The vase was empty");
                }
            }
        }
        return new Result(false, "There is no vase here");
    }

    public Result plantBowlingNut(int index, int row, int col) {
        GameSession session = GameSession.getInstance();
        BowlingLevel level = (BowlingLevel) session.getCurrentMode();

        if (!level.isBehindRedLine(col))
            return new Result(false, "You must plant behind the red line!");


        Plant nut = level.consumePlant(index);
        if (nut == null) return new Result(false, "No plant at this index!");

        ProjectileType type;
        int damage = 20;

        if (nut.getName().equalsIgnoreCase("wall-nut"))
            type = ProjectileType.WALLNUT_BOWL;
        else if (nut.getName().equalsIgnoreCase("explode-o-nut"))
            type = ProjectileType.EXPLODE_NUT_BOWL;
        else
            type = ProjectileType.GIANT_NUT_BOWL;


        Projectile bowl = Projectile.spawnNewProjectile(
                nut,
                type,
                damage,
                new Position(col, row),
                1,
                0,
                false,
                true
        );

        bowl.setBouncesLeft(Integer.MAX_VALUE); //can collide infinitely

        return new Result(true, "Bowled a " + nut.getName() + "!");
    }


    public Result handlePutZombie(String zombieAlias, int row, int col) {
        GameSession session = GameSession.getInstance();

        if (!(session.getCurrentMode() instanceof IZombieLevel level))
            return new Result(false, "You can only spawn zombies in iZombie minigame!");

        if (!level.isValidZombiePlacement(col))
            return new Result(false, "Invalid placement! You must place zombies behind the red line"+
                    " (Col " + level.getRedLineCol() + " or greater).");

        ZombieType type = ZombieType.fromAlias(zombieAlias);
        Zombie newZombie = InGameEntityGenerator.getZombieForGame(type, row);

        int cost = newZombie.getWaveCost();

        if (session.getCurrentSun() < cost)
            return new Result(false, "Not enough sun! " +
                    "You need " + cost + " but have " + session.getCurrentSun());

        session.addSun(-cost);
        newZombie.setCol(col);
        session.getArena().addZombie(newZombie);
        session.getTimeManager().registerNewTicker(newZombie);
        return new Result(true, zombieAlias + " spawned at row " + row + ", col " + col + "!");

    }


    public Result showMap() {
        GameSession session = GameSession.getInstance();
        Level currentMode = (Level) session.getCurrentMode();
        Arena arena = session.getArena();
        StringBuilder mapDisplay = new StringBuilder();

        if (currentMode instanceof IZombieLevel level) {
            mapDisplay.append("Minigame: iZombie\n");
            mapDisplay.append("Sun: ").append(session.getCurrentSun()).append("\n");
            mapDisplay.append("Red Line Column: ").append(level.getRedLineCol()).append("\n");

            for (int i = 0; i < arena.getRows(); i++) {
                Brain brain = arena.getBrainInRow(i);
                boolean isBrainSafe = (brain != null && !brain.isEaten());
                mapDisplay.append("Brain row ").append(i).append(": ")
                        .append(isBrainSafe ? "safe" : "eaten").append("\n");
            }

        } else if (currentMode instanceof BowlingLevel level) {
            mapDisplay.append("Minigame: Bowling\n");
            mapDisplay.append("Conveyor Belt: ");

            if (level.getBelt().isEmpty()) {
                mapDisplay.append("Empty\n");
            } else {
                for (int i = 0; i < level.getBelt().size(); i++) {
                    mapDisplay.append("[").append(i).append("] ")
                            .append(level.getBelt().get(i).getName()).append("  ");
                }
                mapDisplay.append("\n");
            }

        } else if (currentMode instanceof VaseBreakerLevel) {
            mapDisplay.append("Minigame: Vasebreaker\n");
        } else if (currentMode instanceof BeghouledLevel level) {
            mapDisplay.append("Minigame: Beghouled\n");
            mapDisplay.append("Sun: ").append(session.getCurrentSun()).append("\n");
            mapDisplay.append("Matches Progress: ").append(level.getSuccessfulMatches())
                    .append(" / ").append(level.getTargetMatches()).append("\n");
        }

        mapDisplay.append("----------------------------\n");

        makeTheField(arena, mapDisplay);

        return new Result(true, mapDisplay.toString().trim());
    }

    private static void makeTheField(Arena arena, StringBuilder mapDisplay) {
        int rows = arena.getRows();
        int cols = arena.getCols();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Tile tile = arena.getTile(row, col);

                mapDisplay.append("Tile ").append(row).append(" / ").append(col).append(":\n");

                if (tile instanceof VaseTile vase) {
                    mapDisplay.append("-vase: ").append(vase.isBroken() ? "Broken" : "Intact").append("\n");
                }

                List<Zombie> zombiesInTile = new ArrayList<>();
                if (arena.zombieInRow(row) != null) {
                    for (Zombie z : arena.zombieInRow(row)) {
                        int zombieCol = (int) (z.getX() / PhysicalConstants.TILE_UNIT_LENGTH);
                        if (!z.isDead() && zombieCol == col) {
                            zombiesInTile.add(z);
                        }
                    }
                }

                mapDisplay.append("-zombies:\n");
                addZombiesToMap(mapDisplay, zombiesInTile);

                mapDisplay.append("-plants:\n");
                addPlantToMap(mapDisplay, tile);
            }
        }
    }

    private static void addPlantToMap(StringBuilder mapDisplay, Tile tile) {
        if (tile == null || tile.getPlants() == null || tile.getPlants().isEmpty()) {
            mapDisplay.append("    -None\n");
        } else {
            mapDisplay.append("    -");
            List<Plant> plantsInTile = tile.getPlants();
            for (int k = 0; k < plantsInTile.size(); k++) {
                mapDisplay.append(plantsInTile.get(k).getName());

                if (k < plantsInTile.size() - 1) {
                    mapDisplay.append(", ");
                }
            }
            mapDisplay.append("\n");
        }
    }

    private static void addZombiesToMap(StringBuilder mapDisplay, List<Zombie> zombiesInTile) {
        if (zombiesInTile.isEmpty()) {
            mapDisplay.append("     -None\n");
        } else {
            mapDisplay.append("     -");
            for (int k = 0; k < zombiesInTile.size(); k++) {
                Zombie z = zombiesInTile.get(k);
                mapDisplay.append(z.getName()).append(":")
                        .append((int) (z.getX() / PhysicalConstants.TILE_UNIT_LENGTH)).append(",")
                        .append(z.getRow());

                if (k < zombiesInTile.size() - 1) {
                    mapDisplay.append(", ");
                }
            }
            mapDisplay.append("\n");
        }
    }


    public Result swapPlants(String x1Str, String y1Str, String x2Str, String y2Str) {
        GameSession session = GameSession.getInstance();

        if (!(session.getCurrentMode() instanceof BeghouledLevel)) {
            return new Result(false, "You can only swap plants in the Beghouled minigame!");
        }

        int x1, y1, x2, y2;
        try {
            x1 = Integer.parseInt(x1Str);
            y1 = Integer.parseInt(y1Str);
            x2 = Integer.parseInt(x2Str);
            y2 = Integer.parseInt(y2Str);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinates! Please enter valid integer numbers.");
        }

        BeghouledManager manager = new BeghouledManager();
        String response = manager.swapPlants(y1-1,x1-1,y2-1,x2-1);

        boolean isSuccess = response.startsWith("Match found") || response.startsWith("Cascade");
        return new Result(isSuccess, response);
    }

    public Result upgradeBeghouledPlants(String plantName) {
        GameSession session = GameSession.getInstance();

        if (!(session.getCurrentMode() instanceof BeghouledLevel level)) {
            return new Result(false, "You can only upgrade plants like this in the Beghouled minigame!");
        }

        String response = level.upgradePlants(plantName);
        boolean isSuccess = response.startsWith("Successfully");
        return new Result(isSuccess, response);
    }

    public Result showBeghouledPlants() {
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

        java.util.List<String> activePlants = arena.getActivePlants().stream()
                .map(models.entities.plants.Plant::getName)
                .distinct()
                .toList();

        String plantNames = activePlants.isEmpty() ? "None" : String.join(", ", activePlants);

        sb.append("\nLegend: [xx] Plant Initials | [*] Zombie | [-] Projectile | [s] Sun | [O] Crater\n");
        sb.append("Active Plants: ").append(plantNames).append("\n");

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
                if (pos >= 0 && pos + 1 < rowContent.length) {
                    String name = p.getName();
                    rowContent[pos] = name.length() > 0 ? name.charAt(0) : '+';
                    rowContent[pos + 1] = name.length() > 1 ? name.charAt(1) : ' ';
                }
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
}
