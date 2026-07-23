package models.quest.conditions;

import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.plants.PlantCategory;
import models.game.events.GameEvent;
import models.game.events.GameEventPayload;

public class WinWithThatCategoryCondition extends QuestCondition {
    @JsonProperty("lost")
    private boolean lost = false;

    @JsonProperty("withOrWithout")
    private boolean withOrWithout; // true for just the family and false for without the family

    @JsonProperty("plantCategory")
    private PlantCategory plantCategory;

    @JsonProperty("levelCompleted")
    private boolean levelCompleted = false;

    public WinWithThatCategoryCondition(PlantCategory category, boolean modifier) {
        this.plantCategory = category;
        this.withOrWithout = modifier;
    }

    public WinWithThatCategoryCondition() {
    }

    @Override
    public void updateProgress(GameEventPayload payload) {
        GameEvent event = payload.getType();

        if (event == GameEvent.LEVEL_COMPLETED) {
            levelCompleted = true;
            return;
        }

        if (event == GameEvent.PLANT_PLACED && !lost) {

            if (withOrWithout) {
                if (payload.getPlant().getCategory() != plantCategory) {
                    lost = true;
                }
            } else {

                if (payload.getPlant().getCategory() == plantCategory) {
                    lost = true;
                }
            }
        }
    }

    @Override
    public boolean isHappened() {
        return levelCompleted && !lost;
    }

    public boolean isLost() {
        return lost;
    }

    public void setLost(boolean lost) {
        this.lost = lost;
    }

    public boolean isWithOrWithout() {
        return withOrWithout;
    }

    public void setWithOrWithout(boolean withOrWithout) {
        this.withOrWithout = withOrWithout;
    }

    public PlantCategory getPlantCategory() {
        return plantCategory;
    }

    public void setPlantCategory(PlantCategory plantCategory) {
        this.plantCategory = plantCategory;
    }

    public boolean isLevelCompleted() {
        return levelCompleted;
    }

    public void setLevelCompleted(boolean levelCompleted) {
        this.levelCompleted = levelCompleted;
    }
}
