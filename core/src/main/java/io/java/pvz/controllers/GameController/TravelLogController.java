package io.java.pvz.controllers.GameController;

import io.java.pvz.controllers.NavigationController;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.minigame.BowlingLevel;
import io.java.pvz.models.game.minigame.MiniGameFactory;
import io.java.pvz.models.game.minigame.MiniGameType;
import io.java.pvz.models.quest.Quest;
import io.java.pvz.models.quest.QuestCategory;
import io.java.pvz.models.users.User;

import java.util.List;

public class TravelLogController {
    private User activeUser;
    private ValidPageNames currentPage = ValidPageNames.DAILY;

    public TravelLogController() {}

    public Result changePage(String pageName) {
        activeUser = App.getActiveUser();
        pageName = pageName.toUpperCase();
        ValidPageNames nextPage = findByName(pageName);
        if (nextPage == null) {
            return new Result(false, "Invalid page name\nValid page names:\n" +
                    "1.daily\n2.main\n3.epic\n4.minigame");
        }
        if (nextPage == currentPage) {
            return new Result(false, "You are already in this page.");
        }
        currentPage = nextPage;
        return new Result(true, "Changed the page to the " + currentPage.name().toLowerCase());

    }

    public Result startMiniGame(String miniGameName, String levelString) {
        int levelNumber;
        try {
            levelNumber = Integer.parseInt(levelString);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid level number");
        }
        if (currentPage != ValidPageNames.MINIGAME)
            return new Result(false, "You must be in the MINIGAME page to start a minigame.");

        if (levelNumber < 1 || levelNumber > 3)
            return new Result(false, "Invalid level number! Minigames only have levels 1, 2, and 3.");

        try {

            MiniGameType type = MiniGameType.findByName(miniGameName);

            int maxUnlocked = activeUser.getUnlockedLevelInMinigame(type);
            if (levelNumber - 1 > maxUnlocked)
                return new Result(false, "Level " + levelNumber + " is LOCKED!" +
                        " You must beat level " + (levelNumber - 1) + " first.");

            Level minigameLevel = MiniGameFactory.createLevel(type, levelNumber);
            GameSession.setMinigameLevel(minigameLevel);

            if (!minigameLevel.skipsPlantSelection()) {
                App.setActiveMenu(Menu.PLANTSELLECTION_MENU);

            } else {
                List<Plant> inGamePlants = null;
                if (minigameLevel instanceof BowlingLevel bowling)
                    inGamePlants = bowling.getBelt();

                App.setActiveMenu(Menu.GAME_FLOW_MENU);
                GameSession.startMiniGame(minigameLevel, inGamePlants);
            }

            NavigationController.enterMenu("game flow menu");
            return new Result(true, "Started " + type.getName() + " Level " + (levelNumber) + "! Good luck!");

        } catch (IllegalArgumentException e) {
            return new Result(false,
                    "Invalid minigame name! Available: vasebreaker, bowling, izombie, beghouled, zombotany");
        } catch (NullPointerException e) {
            return new Result(false, "Invalid minigame name!");
        } catch (Exception e) {
            return new Result(false, "Something bad happened. Please try again.");
        }
    }

    enum ValidPageNames {
        DAILY,
        MAIN,
        EPIC,
        MINIGAME;
    }

    private ValidPageNames findByName(String name) {
        for (ValidPageNames page : ValidPageNames.values()) {
            if (page.name().equalsIgnoreCase(name))
                return page;

        }
        return null;
    }

}
