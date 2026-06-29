package models.entities.zombies.behavior.defense;

public class NormalDefense implements DefenseBehavior {
    @Override
    public int mitigateDamage(int damage, String damageType) {
        return damage;
    }

    @Override
    public boolean deflectProjectile(String projectileType) {
        return false;
    }
}
