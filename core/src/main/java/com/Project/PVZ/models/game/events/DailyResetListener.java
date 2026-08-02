package com.Project.PVZ.models.game.events;

import com.Project.PVZ.models.users.User;

public class DailyResetListener implements GameEventListener {

    public DailyResetListener() {
        GameEventMessenger.getInstance().addListener(GameEvent.NEW_DAY_STARTED, this);
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if (event == GameEvent.NEW_DAY_STARTED) {

            User user = com.Project.PVZ.models.App.getActiveUser();
            int daysPassed = payload.getAmount();

            if (user != null) {
                if (user.getQuestManager() != null) {
                    user.getQuestManager().resetDailyQuests();
                }

                user.setHasPlayedDailyChallengeToday(false);
                user.setPurchasedDailyDealToday(false);
                System.out.println("Global Listener: " + daysPassed + " days passed." +
                        " All daily systems have been reset!");
            }
        }
    }
}
