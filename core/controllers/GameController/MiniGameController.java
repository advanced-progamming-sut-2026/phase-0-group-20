package controllers.GameController;

import models.InGameEntityGenerator;
import models.Position;
import models.Result;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieType;
import models.enums.plants.ProjectileType;
import models.fields.tiles.Tile;
import models.fields.tiles.VaseInside;
import models.fields.tiles.VaseTile;
import models.game.Arena;
import models.game.GameSession;
import models.game.adventure.levels.Level;
import models.game.minigame.BowlingLevel;
import models.game.minigame.IZombieLevel;
import models.game.minigame.VaseBreakerLevel;

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
            return new Result(false, "Invalid placement! You must place zombies behind the red line (Col " + level.getRedLineCol() + " or greater).");

        ZombieType type = ZombieType.fromAlias(zombieAlias);
        Zombie newZombie = InGameEntityGenerator.getZombieForGame(type, row);

        int cost = newZombie.getWaveCost();

        if (session.getCurrentSun() < cost)
            return new Result(false, "Not enough sun! You need " + cost + " but have " + session.getCurrentSun());

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
                models.fields.Brain brain = arena.getBrainInRow(i);
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
        }
        mapDisplay.append("----------------------------\n");

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
                        int zombieCol = (int) (z.getX() / models.enums.PhysicalConstants.TILE_UNIT_LENGTH);
                        if (!z.isDead() && zombieCol == col) {
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
                                .append((int) (z.getX() / models.enums.PhysicalConstants.TILE_UNIT_LENGTH)).append(",")
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

}
