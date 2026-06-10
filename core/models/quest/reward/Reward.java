package models.quest.reward;

import models.users.User;

public interface Reward {
    RewardType getRewardType();
    void claimReward(User user);
}
