package models.users;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserWallet {
    private int coin;
    private int diamond;
    private int plantFoodCount;

    public UserWallet() {
        this.coin = 0;
        this.diamond = 0;
        this.plantFoodCount = 0;
    }

    @JsonCreator
    public UserWallet(@JsonProperty("coin") int coin,
                      @JsonProperty("diamond") int diamond,
                      @JsonProperty("plantFoodCount") int plantFoodCount) {
        this.coin = coin;
        this.diamond = diamond;
        this.plantFoodCount = plantFoodCount;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public void costCoin(int amount) {
        this.coin = Math.max(0, this.coin - amount);
    }

    public void earnCoin(int amount) {
        this.coin += amount;
    }

    public int getDiamond() {
        return diamond;
    }

    public void setDiamond(int diamond) {
        this.diamond = diamond;
    }

    public void costDiamond(int amount) {
        this.diamond = Math.max(0, this.diamond - amount);
    }

    public void earnDiamond(int amount) {
        this.diamond += amount;
    }

    public int getPlantFoodCount() {
        return plantFoodCount;
    }

    public void setPlantFoodCount(int plantFoodCount) {
        this.plantFoodCount = plantFoodCount;
    }

    public void addPlantFoodCount(int amount) {
        this.plantFoodCount += amount;
    }
}