package io.java.pvz.controllers.GameController;

import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.quest.Quest;
import io.java.pvz.models.users.User;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

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
                return new Result(false, "Invalid sort type! Valid options: score, minigame, season, quests.");
        }
    }

    public String getCurrentSortType() {
        return currentSortType;
    }

    public List<User> getSortedUsers() {
        List<User> allUsers = App.getAllUsers();
        return switch (currentSortType) {
            case "minigame" -> sortedByMinigame(allUsers);
            case "season" -> sortedBySeason(allUsers);
            case "quests" -> sortedByQuests(allUsers);
            default -> sortedByScore(allUsers);
        };
    }

    private List<User> sortedByScore(List<User> users) {
        return users.stream()
                .sorted(Comparator.comparingInt(User::getLevelsCompleted).reversed()
                        .thenComparing(User::getUsername))
                .collect(Collectors.toList());
    }

    private List<User> sortedByMinigame(List<User> users) {
        return users.stream()
                .sorted(Comparator.comparingInt((User u) ->
                                u.getUnlockedMinigames().values().stream().mapToInt(Integer::intValue).sum()).reversed()
                        .thenComparing(User::getUsername))
                .collect(Collectors.toList());
    }

    private List<User> sortedBySeason(List<User> users) {
        return users.stream()
                .sorted(Comparator.comparingInt(User::getHighestUnlockedChapterIndex).reversed()
                        .thenComparing(Comparator.comparingInt(User::getHighestUnlockedLevelIndex).reversed())
                        .thenComparing(User::getUsername))
                .collect(Collectors.toList());
    }

    private List<User> sortedByQuests(List<User> users) {
        return users.stream()
                .sorted(Comparator.comparingInt((User u) -> u.getQuestManager().getCompletedQuestsCount()).reversed()
                        .thenComparing(User::getUsername))
                .collect(Collectors.toList());
    }
}
