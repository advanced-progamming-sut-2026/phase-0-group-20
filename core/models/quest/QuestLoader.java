package models.quest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class QuestLoader {

    public static List<Quest> loadQuestsFromCSV(String filePath) {
        List<Quest> allQuests = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;
            int rowIndex = 0;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                String[] values = line.split(",");
                if (values.length < 5) continue;

                String title = values[0].trim();
                String categoryStr = values[1].trim();
                String conditionStr = values[2].trim();
                String rewardStr = values[3].trim();
                String priorityStr = values[4].trim();
                String variableStr = values.length > 5 ? values[5].trim() : "";

                QuestCategory category = parseCategory(categoryStr);
                QuestPriority priority = parsePriority(priorityStr);

                Quest quest = new Quest(title, category, priority);

                quest.setCondition(QuestFactory.createCondition(rowIndex, title, conditionStr, variableStr));
                quest.setReward(QuestFactory.createReward(rowIndex, rewardStr, variableStr));

                allQuests.add(quest);
                rowIndex++;
            }
        } catch (Exception e) {
            System.err.println("Error reading quests.csv: " + e.getMessage());
        }
        return allQuests;
    }

    private static QuestPriority parsePriority(String priorityStr) {
        if (priorityStr.contains("بحرانی")) return QuestPriority.CRITICAL;
        if (priorityStr.contains("بالا")) return QuestPriority.HIGH;
        if (priorityStr.contains("متوسط")) return QuestPriority.MEDIUM;
        return QuestPriority.LOW;
    }

    private static QuestCategory parseCategory(String categoryStr) {
        if (categoryStr.contains("روزانه")) return QuestCategory.DAILY;
        if (categoryStr.contains("اصلی")) return // مقدار پیش‌فرض QuestCategory.MAIN;
        if (categoryStr.contains("چالش")) return QuestCategory.EPIC_CHALLENGE;
        return QuestCategory.MINIGAME;
    }
}