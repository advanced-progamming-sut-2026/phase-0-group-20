package models.entities.zombies.behavior.defense;

import models.entities.zombies.Zombie;
import models.entities.zombies.behavior.move.JugglerMove;

public class JugglerDefense implements DefenseBehavior {

    private final Zombie zombie;

    public JugglerDefense(Zombie zombie) {
        this.zombie = zombie;
    }

    @Override
    public int mitigateDamage(int damage, String damageType) {
        return damage;
    }

    @Override
    public boolean deflectProjectile(String projectileType) {
        if (projectileType.equals("STRAIGHT")) { // change later
            if (zombie.getMoveBehavior() instanceof JugglerMove) {
                ((JugglerMove) zombie.getMoveBehavior()).startSpinning();
            }
            return true;
        }
        return false;
    }
}
