package models.entities.obstacle;

import models.game.GameSession;

public class ArcadeMachine extends PushableObstacle {

    public ArcadeMachine(int col, int row) {
        super(PushableObjectType.ARCADE_MACHINE, col, row, 1300);
    }

    @Override
    public void onDestroy() {
        GameSession.notify("Arcade Machine destroyed!");
    }
}
