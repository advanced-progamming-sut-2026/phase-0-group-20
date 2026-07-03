package models.entities;

public class Sun {
    private int amount;


    private int productionTime;
    private int amountProduced;

    public void addSun(int sun) {
        amount += sun;
    }

    public void consumeSun(int sun) {
        amount -= sun;
    }
}
