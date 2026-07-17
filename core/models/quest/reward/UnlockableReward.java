package models.quest.reward;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.App;
import models.entities.plants.Plant;
import models.users.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UnlockableReward implements Reward {
    @JsonProperty("plantToUnlock")
    private Plant plantToUnlock;

    @JsonCreator
    public UnlockableReward(@JsonProperty("plantToUnlock") Plant plantToUnlock) {
        this.plantToUnlock = plantToUnlock;
    }

    @JsonIgnore
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