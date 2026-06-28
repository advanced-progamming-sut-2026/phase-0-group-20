package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class GargantuarMove implements MoveBehavior {
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
        // TODO: logic for throwing imp
    }
}
