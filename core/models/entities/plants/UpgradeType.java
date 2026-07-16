package models.entities.plants;

public enum UpgradeType {
    BUFF_HP {
        @Override
        public void apply(Plant plant, float value, String specialTag) {
            plant.maxHp += (int) value;
            plant.currentHp += (int) value;
            triggerSpecialTagIfPresent(plant, value, specialTag);
        }
    },
    BUFF_DAMAGE {
        @Override
        public void apply(Plant plant, float value, String specialTag) {
            plant.bonusDamage = (int) value;
            triggerSpecialTagIfPresent(plant, value, specialTag);
        }
    },
    BUFF_COST {
        @Override
        public void apply(Plant plant, float value, String specialTag) {
            plant.currentCost += (int) value;
            triggerSpecialTagIfPresent(plant, value, specialTag);
        }
    },
    BUFF_ACTION_INTERVAL {
        @Override
        public void apply(Plant plant, float value, String specialTag) {
            plant.currentActionInterval += value;
            triggerSpecialTagIfPresent(plant, value, specialTag);
        }
    },
    BUFF_RECHARGE {
        @Override
        public void apply(Plant plant, float value, String specialTag) {
            plant.currentRecharge += (int) value;
            triggerSpecialTagIfPresent(plant, value, specialTag);
        }
    },
    SPECIAL_MECHANIC {
        @Override
        public void apply(Plant plant, float value, String specialTag) {
            triggerSpecialTagIfPresent(plant, value, specialTag);
        }
    };

    public abstract void apply(Plant plant, float value, String specialTag);

    protected void triggerSpecialTagIfPresent(Plant plant, float value, String specialTag) {
        if (specialTag != null && !specialTag.trim().isEmpty()) {
            plant.applySpecialMechanic(specialTag, Math.abs(value));
        }
    }
}
