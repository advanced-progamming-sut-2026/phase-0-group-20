package io.java.pvz.models.quest.conditions;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.java.pvz.models.game.events.GameEventPayload;


@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface IQuestCondition {

    boolean isHappened();

    void updateProgress(GameEventPayload payload);

    int getCurrentProgress();

    int getTargetProgress();
}
