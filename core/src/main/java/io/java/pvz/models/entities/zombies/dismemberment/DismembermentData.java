package io.java.pvz.models.entities.zombies.dismemberment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DismembermentData {
    private final String zombieTypeKey;
    private final List<String> headParts;
    private final List<ArmStage> armStages;

    public DismembermentData(String zombieTypeKey, List<String> headParts, List<ArmStage> armStages) {
        this.zombieTypeKey = zombieTypeKey;
        this.headParts = Collections.unmodifiableList(headParts);
        this.armStages = Collections.unmodifiableList(armStages);
    }

    public String getZombieTypeKey() {
        return zombieTypeKey;
    }

    public List<String> getHeadParts() {
        return headParts;
    }

    public boolean hasHeadParts() {
        return headParts != null && !headParts.isEmpty();
    }

    public List<ArmStage> getArmStages() {
        return armStages;
    }

    public boolean hasArmStages() {
        return armStages != null && !armStages.isEmpty();
    }

    public int getArmStageCount() {
        return armStages == null ? 0 : armStages.size();
    }

    public List<String> getArmPartsUpToStage(int stageCount) {
        List<String> result = new ArrayList<>();
        if (armStages == null) return result;
        for (int i = 0; i < stageCount && i < armStages.size(); i++) {
            result.addAll(armStages.get(i).getParts());
        }
        return result;
    }

    @Override
    public String toString() {
        return zombieTypeKey + " [head=" + headParts + ", armStages=" + armStages + "]";
    }

    public static class ArmStage {
        private final List<String> parts;
        private final int healthPercent;
        private final int chancePercent;

        public ArmStage(List<String> parts, int healthPercent, int chancePercent) {
            this.parts = Collections.unmodifiableList(parts);
            this.healthPercent = healthPercent;
            this.chancePercent = chancePercent;
        }

        public List<String> getParts() {
            return parts;
        }

        public int getHealthPercent() {
            return healthPercent;
        }

        public int getChancePercent() {
            return chancePercent;
        }

        @Override
        public String toString() {
            return "ArmStage{parts=" + parts + ", healthPercent=" + healthPercent
                + ", chancePercent=" + chancePercent + "}";
        }
    }
}
