package io.java.pvz.models.quest.reward;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.java.pvz.models.users.User;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface Reward {
    RewardType getRewardType();

    void claimReward(User user);

}
