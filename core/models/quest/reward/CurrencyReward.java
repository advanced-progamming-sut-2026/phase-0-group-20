package models.quest.reward;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.users.User;

public class CurrencyReward implements Reward {
    @JsonProperty("isDiamond")
    private final boolean isDiamond;

    @JsonProperty("amount")
    private final int amount;

    @JsonCreator
    public CurrencyReward(@JsonProperty("isDiamond") boolean isDiamond,
                          @JsonProperty("amount") int amount) {
        this.isDiamond = isDiamond;
        this.amount = amount;
    }

    @JsonIgnore
    @Override
    public RewardType getRewardType() {
        return RewardType.CURRENCY;
    }

    @Override
    public void claimReward(User user) {
        if (isDiamond) {
            user.earnDiamond(amount);
        } else {
            user.earnCoin(amount);
        }
    }

    @Override
    public String toString() {
        String currency = (isDiamond) ? "diamonds" : "coins";
        return amount + " " + currency;
    }
}