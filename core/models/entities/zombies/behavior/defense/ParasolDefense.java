package models.entities.zombies.behavior.defense;

public class ParasolDefense implements DefenseBehavior {
    @Override
    public int mitigateDamage(int damage, String damageType) {
        return damage;
    }

    @Override
    public boolean deflectProjectile(String projectileType) {
        return projectileType.equals("LOBBED"); // it will change later
    }
}
