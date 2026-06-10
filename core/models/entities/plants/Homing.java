package models.entities.plants;

public class Homing extends Plant {

    public Homing(PlantData data) {
        this.id = data.id();
        this.name = data.name();
        this.cost = data.cost();
        this.baseHp = data.baseHp();
        this.currentHp = data.baseHp();
        this.damage = data.damage();
        this.category = data.category();
        this.tags = data.tags();
    }

    @Override
    public void onTick(int currentTick) {

    }
}
