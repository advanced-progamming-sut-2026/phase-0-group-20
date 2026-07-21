package models.quest.reward;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.App;
import models.entities.plants.Plant;
import models.users.User;

public class SeedPackReward implements Reward {
    @JsonProperty("packetCount")
    private final int packetCount;


    @JsonProperty("plantTypeName")
    private String plantTypeName;

    @JsonCreator
    public SeedPackReward(@JsonProperty("plantTypeName") String plantTypeName,
                          @JsonProperty("packetCount") int packetCount) {
        this.plantTypeName = plantTypeName;
        this.packetCount = packetCount;
    }

    public SeedPackReward(Plant plantType, int packetCount) {
        this.plantTypeName = (plantType != null) ? plantType.getName() : null;
        this.packetCount = packetCount;
    }

    @JsonIgnore
    @Override
    public RewardType getRewardType() {
        return RewardType.SEED_PACK;
    }

    @Override
    public void claimReward(User user) {
        Plant targetPlant = null;

        if (plantTypeName == null) {
            targetPlant = determineRandomPlant(user);
        } else {
            targetPlant = App.findPlantByName(plantTypeName);
        }

        if (targetPlant != null) {
            for (int i = 0; i < packetCount; i++) {
                user.getInventory().addSeedPacket(targetPlant);
            }
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
        return packetCount + " seed packets";
    }
}