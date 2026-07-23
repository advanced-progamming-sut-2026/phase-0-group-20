package models.entities.zombies.behavior.attack;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.context.SnorkelContext;

public class SnorkelAttack implements AttackBehavior {
    private final Zombie zombie;
    private final SnorkelContext context;
    private final AttackBehavior normalAttack;

    public SnorkelAttack(Zombie zombie, SnorkelContext context) {
        this.zombie = zombie;
        this.context = context;
        this.normalAttack = new NormalAttack(zombie);
    }

    @Override
    public void execute() {
        if (context.isSubmerged()) {
            context.setSubmerged(false);
            notify("Snorkel Zombie surfaced to eat a plant!");
        }
        normalAttack.execute();
    }
}
