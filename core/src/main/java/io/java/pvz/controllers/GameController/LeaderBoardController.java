package io.java.pvz.controllers.GameController;

import io.java.pvz.models.Result;

public class LeaderBoardController {

    private String currentSortType = "score";

    public Result changeSortType(String sortType) {
        switch (sortType.toLowerCase()) {
            case "score":
            case "minigame":
            case "season":
            case "quests":
                currentSortType = sortType.toLowerCase();
                return new Result(true, "Sort type changed to " + sortType + ".");
            default:
                return new Result(false, "Invalid sort type!");
        }
    }

    public String getCurrentSortType() {
        return currentSortType;
    }
}
