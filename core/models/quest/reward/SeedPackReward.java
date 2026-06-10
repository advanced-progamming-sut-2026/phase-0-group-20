package models.quest.reward;

import models.users.User;

public class SeedPackReward implements Reward {
    @Override
    public RewardType getRewardType() {
        return RewardType.SEED_PACK;
    }

    @Override
    public void claimReward(User user) {

    }
}
