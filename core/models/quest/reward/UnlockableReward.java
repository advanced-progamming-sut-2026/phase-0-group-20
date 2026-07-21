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

    @JsonProperty("plantToUnlockName")
    private String plantToUnlockName;

    @JsonCreator
    public UnlockableReward(@JsonProperty("plantToUnlockName") String plantToUnlockName) {
        this.plantToUnlockName = plantToUnlockName;
    }

    public UnlockableReward(Plant plantToUnlock) {
        this.plantToUnlockName = (plantToUnlock != null) ? plantToUnlock.getName() : null;
    }

    @JsonIgnore
    @Override
    public RewardType getRewardType() {
        return RewardType.UNLOCK_PLANT;
    }

    @Override
    public void claimReward(User user) {
        Plant targetPlant = null;

        if (plantToUnlockName == null) {
            targetPlant = determineLockedPlant(user);
        } else {
            targetPlant = App.findPlantByName(plantToUnlockName);
        }

        if (targetPlant != null && !user.getUnlockedPlants().contains(targetPlant)) {
            user.getUnlockedPlants().add(targetPlant);
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