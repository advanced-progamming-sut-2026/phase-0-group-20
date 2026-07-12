package models.quest;

import models.game.GameEventPayload;

import java.util.List;

public class QuestManager {
    private List<Quest> activeQuests;

    public void checkAllEvents(GameEventPayload payload) {
        for (Quest quest : activeQuests) {
            if (!quest.isCompleted()) {
                quest.onEvent(payload);
            }
        }
    }

    public void addQuest(Quest quest) {
        activeQuests.add(quest);
    }
}
