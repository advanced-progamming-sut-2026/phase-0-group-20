package models.entities.obstacle;

import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieFactory;
import models.entities.zombies.ZombieType;
import models.game.GameSession;

public class Barrel extends PushableObstacle {
    public Barrel(int col, int row) {
        super(PushableObjectType.BARREL, col, row, 1200);
    }

    @Override
    public void onDestroy() {
        GameSession.notify("Barrel destroyed! Spawning 2 Imps...");
        GameSession session = GameSession.getInstance();

        Zombie imp1 = ZombieFactory.create(ZombieType.IMP, this.getRow());
        imp1.setCol(this.getCol());
        imp1.setX(this.getX());
        session.getTimeManager().registerNewTicker(imp1);

        Zombie imp2 = ZombieFactory.create(ZombieType.IMP, this.getRow());
        imp2.setCol(this.getCol());
        imp2.setX(this.getX() + 30);
        session.getTimeManager().registerNewTicker(imp2);


        session.getArena().getActiveZombies().add(imp1);
        session.getArena().getActiveZombies().add(imp2);
    }
}
