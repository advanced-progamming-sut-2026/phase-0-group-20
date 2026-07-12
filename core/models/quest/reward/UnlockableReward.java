package models.quest.reward;

import models.entities.plants.Plant;
import models.users.User;

public class UnlockableReward implements Reward {
    private Plant plantToUnlock;

    public UnlockableReward(Plant plantToUnlock) {
        this.plantToUnlock = plantToUnlock;
    }

    @Override
    public RewardType getRewardType() {
        return RewardType.UNLOCK_PLANT;
    }

    @Override
    public void claimReward(User user) {
        if (plantToUnlock == null) {
            plantToUnlock = determineLockedPlant(user);
        }

        if (plantToUnlock != null && !user.getUnlockedPlants().contains(plantToUnlock)) {
            user.getUnlockedPlants().add(plantToUnlock);
        }
    }

    private Plant determineLockedPlant(User user) {
        return null;
    }
}