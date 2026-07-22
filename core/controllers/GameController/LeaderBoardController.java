package controllers.GameController;

import models.App;
import models.Result;
import models.quest.Quest;
import models.users.User;

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

    public Result showResults() {
        List<User> allUsers = App.getAllUsers();
        for  (User user : allUsers) {
            for (Quest quest : user.getQuestManager().getActiveQuests()){
                if(quest.isCompleted()){
                    System.out.println(quest.getTitle());
                }
            }
        }
        List<User> sortedUsers = switch (currentSortType) {
            case "minigame" -> sortedByMinigame(allUsers);
            case "season" -> sortedBySeason(allUsers);
            case "quests" -> sortedByQuests(allUsers);
            default -> sortedByScore(allUsers);
        };

        if (sortedUsers == null || sortedUsers.isEmpty()) {
            return new Result(false, "No users to display on the leaderboard.");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("--- LEADERBOARD (Sorted by: ").append(currentSortType.toUpperCase()).append(") ---\n");

        for (int i = 0; i < sortedUsers.size(); i++) {
            User u = sortedUsers.get(i);
            sb.append(i + 1).append(". ").append(u.getUsername());

            if (currentSortType.equals("score")) {
                sb.append(" | Score: ").append(u.getHighestBonusScore());
            } else if (currentSortType.equals("season")) {
                sb.append(" | Chapter: ").append(u.getHighestUnlockedChapterIndex() + 1)
                        .append(", Level: ").append(u.getHighestUnlockedLevelIndex() + 1);
            } else if (currentSortType.equals("minigame")) {
                int minigameScore = u.getUnlockedMinigames().size();
                sb.append(" | Minigame Levels: ").append(minigameScore);
            } else if (currentSortType.equals("quests")) {
                sb.append(" | Quests Completed: ").append(u.getQuestManager().getCompletedQuestsCount());
            }
            sb.append("\n");
        }
        return new Result(true, sb.toString().trim());
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
