package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

public class ProspectorMove implements MoveBehavior {
    private final Zombie zombie;
    private boolean dynamiteActive;
    private boolean isBlownToBack;
    private int ticksAlive;
    private final int EXPLOSION_TICK_THRESHOLD = 10 * 60;

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
            zombie.setX((int) (zombie.getX() + zombie.getCurrentSpeed()));
        } else {
            zombie.moveForward();
        }
    }

    private void explodeDynamite() {
        isBlownToBack = true;
        dynamiteActive = false;

        // TODO : move zombie to the end of the line
    }

    public void defuseDynamite() { // if ice shoot -> it will call in defense logic
        dynamiteActive = false;
    }
}
