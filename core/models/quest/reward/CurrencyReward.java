package models.quest.reward;

import models.users.User;

public class CurrencyReward implements Reward {
    private final boolean isDiamond;
    private final int amount;

    public CurrencyReward(boolean isDiamond, int amount) {
        this.isDiamond = isDiamond;
        this.amount = amount;
    }

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