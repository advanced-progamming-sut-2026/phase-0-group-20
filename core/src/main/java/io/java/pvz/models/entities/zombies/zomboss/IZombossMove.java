package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.behavior.move.MoveBehavior;

public interface IZombossMove extends MoveBehavior {
    void onEnter();
//    void execute() on MoveBehavior
    void onExit();
    void reset();
}
