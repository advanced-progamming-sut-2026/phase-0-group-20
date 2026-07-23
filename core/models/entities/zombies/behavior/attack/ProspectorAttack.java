package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.context.ProspectorContext;

public class ProspectorAttack implements AttackBehavior {
    private final AttackBehavior normalAttack;
    private final ProspectorContext context;

    public ProspectorAttack(Zombie zombie, ProspectorContext context) {
        this.normalAttack = new NormalAttack(zombie);
        this.context = context;
    }

    @Override
    public void execute() { // it's totally for graphic
        normalAttack.execute();
    }
}
