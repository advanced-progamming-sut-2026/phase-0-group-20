package io.java.pvz.controllers.GameController;

import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.Position;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.Sun;
import io.java.pvz.models.entities.projectiles.Projectile;
import io.java.pvz.models.entities.projectiles.ProjectileTuning;import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.enums.PhysicalConstants;
import io.java.pvz.models.enums.plants.ProjectileType;
import io.java.pvz.models.fields.Brain;
import io.java.pvz.models.fields.LawnMower;
import io.java.pvz.models.fields.tiles.*;
import io.java.pvz.models.game.Arena;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.minigame.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MiniGameController {

    private Integer parsePositiveInt(String str) {
        try {
            int val = Integer.parseInt(str);
            if (val > 0) return val;
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    public Result breakVase(String x, String y) {
        Integer spawnX = parsePositiveInt(x);
        Integer spawnY = parsePositiveInt(y);
        if (spawnX == null || spawnY == null) return new Result(false, "Invalid coordinate");
        GameSession session = GameSession.getInstance();

        Arena arena = session.getArena();
        Tile tile = arena.getTile(spawnY - 1, spawnX - 1);

        if (tile instanceof VaseTile vase) {
            VaseInside vaseInside = vase.breakVase();

            if (vaseInside == VaseInside.ZOMBIE) {
                Zombie zombieTemplate;

                if (vase instanceof ZombieVaseTile) {
                    zombieTemplate = InGameEntityGenerator.getZombieForGame(ZombieType.GARGANTUAR, spawnY - 1);
                } else {
                    List<Zombie> zList = session.getChosenZombies();
                    zombieTemplate = zList.get(new Random().nextInt(zList.size()));
                }

                Zombie freshZombie = InGameEntityGenerator.getZombieForGame(zombieTemplate.getType(), spawnY - 1);
                freshZombie.setCol(spawnX - 1);
                arena.addZombie(freshZombie);
                session.getTimeManager().registerNewTicker(freshZombie);

                return new Result(true, "A zombie emerged from the vase at [" + spawnX + "][" + spawnY + "]!");

            } else if (vaseInside == VaseInside.SEED_PACKET) {
                java.util.List<Plant> pList = session.getChosenPlants();
                Plant template = pList.get(new Random().nextInt(pList.size()));
                Plant freshPlant = InGameEntityGenerator.getPlantForGame(template, false);

                DroppedSeedPacket packet = new DroppedSeedPacket(freshPlant, spawnY - 1, spawnX - 1);
                arena.getDroppedSeedPackets().add(packet);
                session.getTimeManager().registerNewTicker(packet);

                return new Result(true, "A " + freshPlant.getName() + " seed packet dropped! Plant it quickly.");
            }
            App.getActiveUser().addPlantFoodCount(1);
            return new Result(true, "New PlantFood added");
        }
        return new Result(false, "There is no vase here.");
    }

    public Result plantBowlingNut(Plant nut, int col, int  row) {

        GameSession session = GameSession.getInstance();
        BowlingLevel level = (BowlingLevel) session.getCurrentMode();

        if (!level.isBehindRedLine(col))
            return new Result(false, "You must plant behind the red line!");

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
            new Position(col, row-1),
            ProjectileTuning.BOWLING_SPEED_TILES_PER_SEC,
            0,
            false,
            true
        );

        bowl.setBouncesLeft(Integer.MAX_VALUE);
        level.getBelt().remove(nut);
        return new Result(true, "Bowled a " + nut.getName() + "! in " + col + " " + row);
    }


    public Result handlePutZombie(String zombieAlias, String colStr, String rowStr) {
        Integer row = parsePositiveInt(rowStr);
        Integer col = parsePositiveInt(colStr);
        if (row == null || col == null) return new Result(false, "Invalid coordinates");

        GameSession session = GameSession.getInstance();

        if (!(session.getCurrentMode() instanceof IZombieLevel level))
            return new Result(false, "You can only spawn zombies in iZombie minigame!");

        if (!level.isValidZombiePlacement(col - 1))
            return new Result(false, "Invalid placement! You must place zombies behind the red line" +
                " (Col " + (level.getRedLineCol() + 1)+ " or greater).");

        ZombieType type = ZombieType.fromAlias(zombieAlias);

        if (type == null)
            return new Result(false, "Invalid zombie type: " + zombieAlias);

        if (!level.getZombiesForThisLevel().contains(type))
            return new Result(false, "You cannot use " + zombieAlias +
                " in this level! Check your available zombies.");

        Zombie newZombie = InGameEntityGenerator.getZombieForGame(type, row - 1);

        int cost = newZombie.getWaveCost();

        if (session.getCurrentSun() < cost)
            return new Result(false, "Not enough sun! " +
                "You need " + cost + " but have " + session.getCurrentSun());

        session.addSun(-cost);
        newZombie.setCol(col - 1);
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

                mapDisplay.append("Tile ").append(col + 1).append(" / ").append(row + 1).append(":\n");

                if (tile instanceof VaseTile vase) {
                    mapDisplay.append("-vase: ").append(vase.isBroken() ? "Broken" : "Intact").append("\n");
                }

                List<Zombie> zombiesInTile = new ArrayList<>();
                if (arena.zombieInRow(row) != null) {
                    for (Zombie z : arena.zombieInRow(row)) {
                        int zombieCol = (int) (z.getX() - PhysicalConstants.GRID_START_X / PhysicalConstants.TILE_WIDTH);
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
                    .append(z.getCol() + 1).append(" , ")
                    .append(z.getRow() + 1);

                if (k < zombiesInTile.size() - 1) {
                    mapDisplay.append(", ");
                }
            }
            mapDisplay.append("\n");
        }
    }


    public Result swapPlants(int col1 , int row1 , int col2, int row2) {
        GameSession session = GameSession.getInstance();

        if (!(session.getCurrentMode() instanceof BeghouledLevel)) {
            return new Result(false, "You can only swap plants in the Beghouled minigame!");
        }

        BeghouledManager manager = ((BeghouledLevel) session.getCurrentMode()).getManager();
        String response = manager.swapPlants(row1 - 1 , col1 - 1 , row2 - 1 , col2 - 1);

        boolean isSuccess = response.startsWith("Match found") || response.startsWith("Cascade");
        return new Result(isSuccess, response);
    }

    public Plant getPlantAtTile(int col , int row ){
        Arena arena = GameSession.getInstance().getArena();
        if (arena.getTile(row-1,col-1 ).getPlants().isEmpty()) {
            return null;
        }
        Plant plant = arena.getTile(row-1,col-1).getPlants().getFirst();
        return plant;
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

        List<String> activePlants = arena.getActivePlants().stream()
            .map(Plant::getName)
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

    public Result plantFromVase(String sourceX, String sourceY, String destX, String destY) {
        Integer srcX = parsePositiveInt(sourceX);
        Integer srcY = parsePositiveInt(sourceY);
        Integer dstX = parsePositiveInt(destX);
        Integer dstY = parsePositiveInt(destY);

        if (srcX == null || srcY == null || dstX == null || dstY == null)
            return new Result(false, "Invalid coordinates");

        GameSession session = GameSession.getInstance();
        Arena arena = session.getArena();

        DroppedSeedPacket packet = arena.getDroppedSeedPackets().stream()
            .filter(p -> p.getCol() == srcX - 1 && p.getRow() == srcY - 1 && !p.isExpired())
            .findFirst().orElse(null);

        if (packet == null) return new Result(false, "No active seed packet found at this location.");

        Tile destTile = arena.getTile(dstY - 1, dstX - 1);
        if (!destTile.isPlantable(packet.getPlant()))
            return new Result(false, "Cannot plant here.");

        Plant plant = packet.getPlant();
        destTile.addPlant(plant);
        arena.addPlant(plant);
        session.getTimeManager().registerNewTicker(plant);

        packet.setExpired(true);
        arena.getDroppedSeedPackets().remove(packet);
        session.getTimeManager().unregisterTicker(packet);

        return new Result(true, "Successfully planted " + plant.getName() + "!");
    }
}
