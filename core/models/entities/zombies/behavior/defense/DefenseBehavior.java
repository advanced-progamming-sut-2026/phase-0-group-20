package models.entities.zombies.behavior.defense;

public interface DefenseBehavior {
    int mitigateDamage(int damage, String damageType); // return damage after defense

    boolean deflectProjectile(String projectileType);
}
