package models.entities.zombies.behavior.defense;

public class DragonImpDefense implements DefenseBehavior {
    @Override
    public int mitigateDamage(int damage, String damageType) {
        if (damageType.equals("FIRE")) {
            return 0;
        }
        return damage;
    }

    @Override
    public boolean deflectProjectile(String projectileType) {
        return false;
    }
}
