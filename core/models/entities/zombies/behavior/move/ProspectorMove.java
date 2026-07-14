package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;
import models.game.GameSession;

public class ProspectorMove implements MoveBehavior {
    private final Zombie zombie;
    private final int EXPLOSION_TICK_THRESHOLD = 10 * 60;
    private boolean dynamiteActive;
    private boolean isBlownToBack;
    private int ticksAlive;

    public ProspectorMove(Zombie zombie) {
        this.zombie = zombie;
        this.dynamiteActive = true;
        this.isBlownToBack = false;
        this.ticksAlive = 0;
    }


    @Override
    public void execute() {
        if (dynamiteActive && !isBlownToBack) {
            ticksAlive++;
            if (ticksAlive >= EXPLOSION_TICK_THRESHOLD) {
                explodeDynamite();
            }
        }

        if (isBlownToBack) {
            zombie.getPosition().moveX(zombie.getCurrentSpeed());
        } else {
            zombie.moveForward();
        }
    }

    private void explodeDynamite() {
        isBlownToBack = true;
        dynamiteActive = false;

        int lastCol = GameSession.getInstance().getArena().getCols() - 1;
        zombie.setCol(lastCol);

        System.out.println(zombie.getName() + "'s dynamite exploded! Now heading back the other way.");
    }

    public void defuseDynamite() { // if ice shoot -> it will call in defense logic
        dynamiteActive = false;
    }

    public boolean isBlownToBack() {
        return isBlownToBack;
    }
}
