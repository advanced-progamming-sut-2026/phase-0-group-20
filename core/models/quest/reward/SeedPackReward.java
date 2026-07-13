package models.quest.reward;

import models.entities.plants.Plant;
import models.users.User;

public class SeedPackReward implements Reward {
    private final int packetCount;
    private Plant plantType;

    public SeedPackReward(Plant plantType, int packetCount) {
        this.plantType = plantType;
        this.packetCount = packetCount;
    }

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