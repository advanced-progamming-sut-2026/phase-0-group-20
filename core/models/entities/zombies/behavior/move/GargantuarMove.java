package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieFactory;
import models.entities.zombies.ZombieType;
import models.game.GameSession;

public class GargantuarMove implements MoveBehavior {
    private static final int IMP_LANDING_COLUMN = 2;

    private final Zombie zombie;
    private boolean impThrown;

    public GargantuarMove(Zombie zombie) {
        this.zombie = zombie;
        this.impThrown = false;
    }

    @Override
    public void execute() {
        if (!impThrown && zombie.getHealth() <= zombie.getBaseHp() / 2) {
            throwImp();
            impThrown = true;
        }

        zombie.moveForward();
    }

    private void throwImp() {
        Zombie imp = ZombieFactory.create(ZombieType.IMP, zombie.getRow());
        imp.setCol(IMP_LANDING_COLUMN);

        GameSession session = GameSession.getInstance();
        session.getTimeManager().registerNewTicker(imp);
        session.getArena().addZombie(imp);

        notify(zombie.getName() + " threw its Imp onto column "
                + (IMP_LANDING_COLUMN + 1) + " of row " + zombie.getRow() + "!");
    }
}
