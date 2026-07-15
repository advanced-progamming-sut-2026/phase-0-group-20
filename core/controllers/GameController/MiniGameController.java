package controllers.GameController;

import models.Result;
import models.fields.tiles.Tile;
import models.fields.tiles.VaseInside;
import models.fields.tiles.VaseTile;
import models.game.GameSession;

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

}
