package models.entities.plants;

public interface IPlant {
    int getId();
    String getName();
    String getCategory();
    String[] getTags();
    int getCost();
    int getBaseHp();
    int getDamage();
    String getBaseAbility();
}
