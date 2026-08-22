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

        if (!level.isBehindRedLine(col - 1))
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
            new Position(col - 1, row-1),
            ProjectileTuning.BOWLING_SPEED_TILES_PER_SEC,
            0,
            false,
            false
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
