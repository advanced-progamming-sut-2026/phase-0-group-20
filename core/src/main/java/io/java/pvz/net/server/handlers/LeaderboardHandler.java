package io.java.pvz.net.server.handlers;

import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.users.User;
import io.java.pvz.net.protocol.NetworkMessage;
import io.java.pvz.net.server.ClientConnection;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeaderboardHandler {

    public NetworkMessage leaderboardRequest(NetworkMessage request, ClientConnection connection) {
        String sortType = request.getString("sortType");
        if (sortType == null) sortType = "score";
        sortType = sortType.toLowerCase();

        List<User> users = new ArrayList<>(DataBaseManager.getAllUsers());
        List<User> sorted = switch (sortType) {
            case "minigame" -> sortedByMinigame(users);
            case "season" -> sortedBySeason(users);
            case "quests" -> sortedByQuests(users);
            default -> sortedByScore(users);
        };

        List<Map<String, Object>> rows = new ArrayList<>();
        for (User u : sorted) {
            Map<String, Object> row = new HashMap<>();
            row.put("username", u.getUsername());
            row.put("myPoint", u.getHighestBonusScore());
            row.put("chapter", u.getHighestUnlockedChapterIndex() + 1);
            row.put("level", u.getHighestUnlockedLevelIndex() + 1);

            int totalMinigameLevels = u.getUnlockedMinigames().values().stream().mapToInt(Integer::intValue).sum();
            row.put("minigameLevelsUnlocked", totalMinigameLevels);

            row.put("questsCompleted", u.getQuestManager().getLeaderBoardResult());
            rows.add(row);
        }

        NetworkMessage response = NetworkMessage.success(request);
        response.put("sortType", sortType);
        response.put("rows", rows);
        return response;
    }

    public NetworkMessage scoreSubmit(NetworkMessage request, ClientConnection connection) {
        User user = connection.getAuthenticatedUser();
        if (user == null) {
            return NetworkMessage.failure(request, "not logged in");
        }

        Integer score = request.getInt("score");
        if (score == null || score < 0) {
            return NetworkMessage.failure(request, "invalid score");
        }

        boolean isNewRecord = score > user.getHighestBonusScore();
        if (isNewRecord) {
            user.setHighestBonusScore(score);
            DataBaseManager.saveOrUpdateUser(user);
        }

        NetworkMessage response = NetworkMessage.success(request);
        response.put("newRecord", isNewRecord);
        response.put("myPoint", user.getHighestBonusScore());
        return response;
    }

    private List<User> sortedByScore(List<User> users) {
        return users.stream()
            .sorted(Comparator.comparingInt(User::getHighestBonusScore).reversed()
                .thenComparing(User::getUsername))
            .toList();
    }

    private List<User> sortedByMinigame(List<User> users) {
        return users.stream()
            .sorted(Comparator.comparingInt((User u) ->
                u.getUnlockedMinigames().values().stream().mapToInt(Integer::intValue).sum()
            ).reversed().thenComparing(User::getUsername))
            .toList();
    }

    private List<User> sortedBySeason(List<User> users) {
        return users.stream()
            .sorted(Comparator.comparingInt(User::getHighestUnlockedChapterIndex).reversed()
                .thenComparing(Comparator.comparingInt(User::getHighestUnlockedLevelIndex).reversed())
                .thenComparing(User::getUsername))
            .toList();
    }

    private List<User> sortedByQuests(List<User> users) {
        return users.stream()
            .sorted(Comparator.comparingInt((User u) -> u.getQuestManager().getCompletedQuestsCount()).reversed()
                .thenComparing(User::getUsername))
            .toList();
    }
}
