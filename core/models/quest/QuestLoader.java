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

            for (JsonElement element : jsonArray) {
                JsonObject jsonObject = element.getAsJsonObject();

                Quest quest = QuestFactory.buildQuest(jsonObject);

                if (quest != null) {
                    allQuests.add(quest);
                }
            }
        } catch (Exception e) {
            System.err.println("Error reading quests.json: " + e.getMessage());
        }

        return allQuests;
    }
}