package io.java.pvz.models.entities.obstacle;

import io.java.pvz.models.game.GameSession;

public class IceBlock extends PushableObstacle {

    public IceBlock(int col, int row) {
        super(PushableObjectType.ICE_BLOCK, col, row, 1000);
    }

    @Override
    public void onDestroy() {
        GameSession.notify("Ice Block shattered into pieces!");
    }
}
