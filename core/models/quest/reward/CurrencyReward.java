package models.quest.reward;

import models.users.User;

public class CurrencyReward implements Reward {
    @Override
    public RewardType getRewardType() {
        return RewardType.CURRENCY;
    }

    @Override
    public void claimReward(User user) {

    }
}
