package models.entities.plants;

public class Melee extends  Plant {
    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public String getCategory() {
        return "";
    }

    @Override
    public String[] getTags() {
        return new String[0];
    }

    @Override
    public int getCost() {
        return 0;
    }

    @Override
    public int getBaseHp() {
        return 0;
    }

    @Override
    public int getDamage() {
        return 0;
    }

    @Override
    public String getBaseAbility() {
        return "";
    }
}
