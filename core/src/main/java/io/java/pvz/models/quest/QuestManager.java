package io.java.pvz.models.quest;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import io.java.pvz.models.App;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventListener;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.quest.conditions.QuestCondition;

import java.util.ArrayList;
import java.util.List;

public class QuestManager implements GameEventListener {

    private final List<Quest> activeQuests = new ArrayList<>();

    public QuestManager() {
        registerToAllEvents();
    }

    @JsonCreator
    public QuestManager(@JsonProperty("activeQuests") List<Quest> activeQuests) {
        if (activeQuests != null) {
            this.activeQuests.addAll(activeQuests);
        }
        registerToAllEvents();
    }


    private void registerToAllEvents() {
        GameEventMessenger messenger = GameEventMessenger.getInstance();
        for (GameEvent event : GameEvent.values()) {
            messenger.addListener(event, this);
        }
    }

    public void unregisterFromAllEvents() {
        GameEventMessenger messenger = GameEventMessenger.getInstance();
        for (GameEvent event : GameEvent.values()) {
            messenger.removeListener(event, this);
        }
    }


    public void resetDailyQuests() {
        for (Quest quest : activeQuests) {
            if (!quest.isCompleted() && quest.getCategory() == QuestCategory.DAILY) {
                if (quest.getCondition() instanceof QuestCondition condition) {
                    condition.resetCurrentProgress();

                }
            }
        }
    }

    public void resetOneMissionQuests() {
        for (Quest quest : activeQuests) {
            if (!quest.isCompleted() && quest.isOnMission()) {
                if (quest.getCondition() instanceof QuestCondition condition) {
                    condition.resetCurrentProgress();

                }
            }
        }
    }


    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {

        if (App.getActiveUser() != null && App.getActiveUser().getQuestManager() != this) {
            this.unregisterFromAllEvents();
            return;
        }
        for (Quest quest : activeQuests) {
            if (!quest.isCompleted()) {
                quest.onEvent(payload);
            }
        }

    }

    @JsonIgnore
    public int getCompletedQuestsCount() {
        int count = 0;
        for (Quest quest : activeQuests) {
            if (quest.isCompleted()) {
                count++;
            }
        }
        return count;
    }


    public List<Quest> getActiveQuests() {
        return activeQuests;
    }

    @JsonSetter
    public void setActiveQuests(List<Quest> quests) {
        if (quests != null) {
            this.activeQuests.clear();
            this.activeQuests.addAll(quests);
        }
    }

    public void addQuest(Quest quest) {
        this.activeQuests.add(quest);
    }
}
