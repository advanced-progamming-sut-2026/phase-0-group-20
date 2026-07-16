package controllers.GameController;

import models.Position;
import models.Result;
import models.entities.plants.Plant;
import models.entities.projectiles.Projectile;
import models.enums.plants.ProjectileType;
import models.fields.tiles.Tile;
import models.fields.tiles.VaseInside;
import models.fields.tiles.VaseTile;
import models.game.GameSession;
import models.game.minigame.BowlingLevel;
import models.game.minigame.VaseBreakerLevel;

public class MiniGameController {

    public Result changeMenu(String newMenu) {
        return null;
    }

    public Result handleRule(String newMenu) {
        return null;
    }


    public Result enterVaseBreaker(int levelNumber) {

        GameSession session = GameSession.getInstance();
        VaseBreakerLevel vaseLevel = new VaseBreakerLevel("Vasebreaker Level " + levelNumber, levelNumber);
        session.setCurrentMode(vaseLevel);
        vaseLevel.onStart(session);
//        NavigationController.enterMenu(); //varede che menu e beshim? aslsa varede menu beshim ya haminja ye menu jadid bashe?
        return new Result(true, "Vasebreaker started! Good luck!");

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

}
