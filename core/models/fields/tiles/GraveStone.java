package models.fields.tiles;

public class GraveStone {
    private int hp = 700;
    private final boolean hasSun;
    private final boolean hasPlantFood;
    

    public GraveStone() {
        this.hasSun = false;
        this.hasPlantFood = false;
    }

    public GraveStone(boolean hasSun, boolean hasPlantFood) {
        this.hasSun = hasSun;
        this.hasPlantFood = hasPlantFood;
    }

    public void takeDamage(int damage) {
        this.hp -= damage;
    }

    public int getHp() {
        return hp;
    }

    public boolean hasSun() {
        return hasSun;
    }

    public boolean hasPlantFood() {
        return hasPlantFood;
    }
}
