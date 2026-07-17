package models.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class QuestLoader {

    public static List<Quest> loadQuestsFromJson(String filePath) {
        List<Quest> allQuests = new ArrayList<>();

        try (Reader reader = new FileReader(filePath)) {
            JsonArray jsonArray = JsonParser.parseReader(reader).getAsJsonArray();
            int rowIndex = 0;

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();
                String title = jsonObject.has("Title") && !jsonObject.get("Title").isJsonNull() ? jsonObject.get("Title").getAsString() : "";
                String categoryStr = jsonObject.has("Category") && !jsonObject.get("Category").isJsonNull() ? jsonObject.get("Category").getAsString() : "";
                String conditionStr = jsonObject.has("Condition") && !jsonObject.get("Condition").isJsonNull() ? jsonObject.get("Condition").getAsString() : "";
                String rewardStr = jsonObject.has("Reward") && !jsonObject.get("Reward").isJsonNull() ? jsonObject.get("Reward").getAsString() : "";
                String priorityStr = jsonObject.has("Priority") && !jsonObject.get("Priority").isJsonNull() ? jsonObject.get("Priority").getAsString() : "";
                String variableStr = jsonObject.has("Variables") && !jsonObject.get("Variables").isJsonNull() ? jsonObject.get("Variables").getAsString() : "";
                boolean onMission = jsonObject.has("onMission") && !jsonObject.get("onMission").isJsonNull() && jsonObject.get("onMission").getAsBoolean();
                QuestCategory category = parseCategory(categoryStr);
                QuestPriority priority = parsePriority(priorityStr);

                Quest quest = new Quest(title, category, priority,onMission , conditionStr);

                quest.setCondition(QuestFactory.createCondition(rowIndex, title, conditionStr, variableStr));
                quest.setReward(QuestFactory.createReward(rowIndex, rewardStr, variableStr));

                allQuests.add(quest);
                rowIndex++;
            }
        } catch (Exception e) {
            System.err.println("Error reading quests.json: " + e.getMessage());
        }
        return allQuests;
    }

    private static QuestPriority parsePriority(String priorityStr) {
        if (priorityStr.equalsIgnoreCase("Critical")) return QuestPriority.CRITICAL;
        if (priorityStr.equalsIgnoreCase("High")) return QuestPriority.HIGH;
        if (priorityStr.equalsIgnoreCase("Medium")) return QuestPriority.MEDIUM;
        return QuestPriority.LOW;
    }

    private static QuestCategory parseCategory(String categoryStr) {
        if (categoryStr.equalsIgnoreCase("Daily")) return QuestCategory.DAILY;
        if (categoryStr.equalsIgnoreCase("Main")) return QuestCategory.MAIN;
        if (categoryStr.equalsIgnoreCase("Epic")) return QuestCategory.EPIC;
        return QuestCategory.MINIGAME;
    }
}