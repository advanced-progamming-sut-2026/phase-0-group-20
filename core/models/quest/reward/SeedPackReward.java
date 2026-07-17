package models.quest.reward;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.entities.plants.Plant;
import models.users.User;

public class SeedPackReward implements Reward {
    @JsonProperty("packetCount")
    private final int packetCount;

    @JsonProperty("plantType")
    private Plant plantType;

    @JsonCreator
    public SeedPackReward(@JsonProperty("plantType") Plant plantType,
                          @JsonProperty("packetCount") int packetCount) {
        this.plantType = plantType;
        this.packetCount = packetCount;
    }

    @JsonIgnore
    @Override
    public RewardType getRewardType() {
        return RewardType.SEED_PACK;
    }

    @Override
    public void claimReward(User user) {
        if (plantType == null) {
            plantType = determineRandomPlant(user);
        }

        for (int i = 0; i < packetCount; i++) {
            user.getInventory().addSeedPacket(plantType);
        }
    }

    private Plant determineRandomPlant(User user) {
        if (user.getUnlockedPlants() != null && !user.getUnlockedPlants().isEmpty()) {
            int randomIndex = (int) (Math.random() * user.getUnlockedPlants().size());
            return user.getUnlockedPlants().get(randomIndex);
        }
        return null;
    }

    @Override
    public String toString() {
        return packetCount + "seed packets";
    }
}