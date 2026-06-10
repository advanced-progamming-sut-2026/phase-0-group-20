package models.quest.conditions;

import models.game.GameEvent;

public class SymmetryLogicCondition extends QuestCondition{
    boolean needsSymmetry;
    @Override
    public void updateProgress(GameEvent event) {

    }
    @Override
    public boolean isHappened()
    {
        return false;
    }
}
