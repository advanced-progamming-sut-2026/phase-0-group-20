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
        zombie.moveForward();

        if (!impThrown && zombie.getHealth() <= (zombie.getBaseHp() / 2)) {
            throwImp();
        }
    }

    private void throwImp() {
        impThrown = true;

        Zombie imp = ZombieFactory.create(ZombieType.IMP, zombie.getRow());
        imp.setCol(IMP_LANDING_COLUMN);

        GameSession session = GameSession.getInstance();


//        session.getArena().spawnZombie(imp); // TODO : SPAWN IMP

        notify(zombie.getName() + " threw its Imp onto column "
                + IMP_LANDING_COLUMN + " of row " + zombie.getRow() + "!");
    }

    public boolean isImpThrown() {
        return impThrown;
    }
}
