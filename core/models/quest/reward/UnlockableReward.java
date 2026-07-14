package models.quest.reward;

import models.App;
import models.entities.plants.Plant;
import models.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        List<Plant> plants = App.getAllPlants();
        List<Plant> userLockedPlants = new ArrayList<>();
        for (Plant plant : plants) {
            if (!user.getUnlockedPlants().contains(plant)) {
                userLockedPlants.add(plant);
            }
        }
        if (userLockedPlants.isEmpty()) {
            System.out.println("You unlocked all the plants.");
            return null;
        }
        int index = new Random().nextInt(userLockedPlants.size());
        return userLockedPlants.get(index);
    }

    @Override
    public String toString() {
        return "a random plant";
    }
}