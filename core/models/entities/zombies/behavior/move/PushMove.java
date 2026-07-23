package models.entities.zombies.behavior.move;

import models.entities.zombies.Zombie;

import java.util.function.BooleanSupplier;

public class PushMove implements MoveBehavior {
    private final Zombie zombie;


    private final BooleanSupplier isObjectAlive;

    private final Runnable pushAction;

    private final MoveBehavior fallbackMove;

    public PushMove(Zombie zombie, BooleanSupplier isObjectAlive, Runnable pushAction, MoveBehavior fallbackMove) {
        this.zombie = zombie;
        this.isObjectAlive = isObjectAlive;
        this.pushAction = pushAction;
        this.fallbackMove = fallbackMove;
    }

    @Override
    public void execute() {
        if (isObjectAlive.getAsBoolean()) {
            pushAction.run();

        } else {
            if (fallbackMove != null) {
                fallbackMove.execute();
            } else {
                zombie.moveForward();
            }
        }
    }
}
