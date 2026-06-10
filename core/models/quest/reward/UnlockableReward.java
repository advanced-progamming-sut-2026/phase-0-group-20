package models.quest.reward;

import models.users.User;

public class UnlockableReward implements Reward {

    @Override
    public void claimReward(User user) {

    }

    @Override
    public RewardType getRewardType() {
        return RewardType.UNLOCK_PLANT;
    }
}
