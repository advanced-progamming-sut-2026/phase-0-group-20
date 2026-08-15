package io.java.pvz.models.entities.zombies.zomboss;

import io.java.pvz.models.entities.zombies.behavior.attack.AttackBehavior;

public interface IZombossAttack extends AttackBehavior {
    void onEnter();
//    execute() on Attack Behavior
    void onExit();

}
