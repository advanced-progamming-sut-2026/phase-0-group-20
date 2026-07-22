package models.quest;

import com.google.gson.JsonObject;
import models.entities.plants.Plant;
import models.entities.plants.PlantFactory;
import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;
import models.quest.conditions.*;
import models.quest.reward.*;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestFactory {

    private static final Random random = new Random();

    public static Quest buildQuest(JsonObject jsonObject) {
        String title = extractString(jsonObject, "Title");
        String categoryStr = extractString(jsonObject, "Category");
        String conditionStr = extractString(jsonObject, "Condition");
        String rewardStr = extractString(jsonObject, "Reward");
        String priorityStr = extractString(jsonObject, "Priority");
        String variableStr = extractString(jsonObject, "Variables");
        boolean onMission = jsonObject.has("onMission") && !jsonObject.get("onMission").isJsonNull() && jsonObject.get("onMission").getAsBoolean();

        QuestCategory category = parseCategory(categoryStr);
        QuestPriority priority = parsePriority(priorityStr);

        QuestCondition condition = null;
        Reward reward = null;

        switch (title) {
            case "Daily Sunblock" -> {
                int targetSun = pickRandomFromDashSeparated(variableStr, 3000);
                conditionStr = conditionStr.replace("sun_amount", String.valueOf(targetSun));
                rewardStr = rewardStr.replace("sun_amount / 100", String.valueOf(targetSun / 100));
                condition = new SunCollectCondition(targetSun);
                reward = new CurrencyReward(false, targetSun / 100);
            }
            case "Chapter Hunter" -> {
                condition = new KillZombieCondition(50, "Random_Chapter");
                reward = new SeedPackReward(PlantFactory.create(new Random().nextInt(64)+1) , 10);
            }
            case "Pro Plant Player" -> {
                condition = new KillZombieCondition(10, "Shooter");
                reward = new UnlockableReward(PlantFactory.create(new Random().nextInt(64)+1));
            }
            case "Only Cactus" -> {
                condition = new KillZombieCondition(10, "Cactus");
                reward = new CurrencyReward(true, 20);
            }
            case "Economical Herbivore" -> {
                int n = pickRandomFromDashSeparated(variableStr, 2);
                conditionStr = conditionStr.replace(" n ", " "+String.valueOf(n)+" ");
                rewardStr = rewardStr.replace("20 - n", String.valueOf(20 - n));
                condition = new MaxPlantLossCondition(n);
                reward = new SeedPackReward(PlantFactory.create(new Random().nextInt(64)+1), Math.max(1, 20 - n));
            }
            case "Defense Master" -> {
                condition = new CertainAmountOfSunCondition(0);
                reward = new CurrencyReward(true, 200);
            }
            case "Quick Reaction" -> {
                condition = new TimeLimitCondition(30, 10);
                reward = new CurrencyReward(false, 500);
            }
            case "Pro Demolition" -> {
                condition = new PlantCategoryUseCondition("explosive", 3);
                reward = new CurrencyReward(false, 100);
            }
            case "Symmetry" -> {
                condition = new SymmetryLogicCondition(true);
                reward = new CurrencyReward(false, 500);
            }
            case "Family Massacre" -> {
                PlantCategory plantCat = PlantCategory.getRandomPlantCategory();
                conditionStr = conditionStr.replace("family_type", plantCat.name());
                condition = new WinWithThatCategoryCondition(plantCat, true);
                reward = new CurrencyReward(false, 1000);
            }
            case "Flourish in Limits" -> {
                PlantCategory plantCat = PlantCategory.getRandomPlantCategory();
                conditionStr = conditionStr.replace("family_type", plantCat.name());
                condition = new WinWithThatCategoryCondition(plantCat, false);
                reward = new CurrencyReward(true, 100);
            }
            case "Night or Morning" -> {
                condition = new WinWithSpecificTagCondition(PlantTag.SHROOM);
                reward = new CurrencyReward(true, 20);
            }
            case "Win Streak" -> {
                condition = new WinStreakCondition(5);
                reward = new CurrencyReward(false, 5000);
            }
            case "Almost Won" -> {
                condition = new KillWithNoLawnmowerCondition(10, 0);
                reward = new CurrencyReward(false, 300);
            }
            case "What OCD?" -> {
                condition = new SymmetryLogicCondition(false);
                reward = new CurrencyReward(false, 800);
            }
            case "Cloudy Day" -> {
                condition = new MaxPlantUsedCondition(PlantCategory.SUN_PRODUCER, 3);
                reward = new CurrencyReward(true, 10);
            }
            case "One Column Less" -> {
                int colIndex = random.nextInt(9);
                conditionStr = conditionStr.replace("nth", String.valueOf(colIndex + 1));
                condition = new EmptyLineCondition(-1, colIndex);
                reward = new CurrencyReward(true, 10);
            }
            case "Defenseless Row" -> {
                int rowIndex = random.nextInt(5);
                conditionStr = conditionStr.replace("nth", String.valueOf(rowIndex + 1));
                condition = new EmptyLineCondition(rowIndex, -1);
                reward = new CurrencyReward(true, 20);
            }
            case "Defenseless Cross" -> {
                int crossIndex = random.nextInt(5);
                conditionStr = conditionStr.replace("nth", String.valueOf(crossIndex + 1));
                condition = new EmptyLineCondition(crossIndex, crossIndex);
                reward = new CurrencyReward(true, 25);
            }
            case "Lawnmower Time" -> {
                int n = pickRandomFromDashSeparated(variableStr, 10);
                conditionStr = conditionStr.replace(" n ", " "+String.valueOf(n)+" ");
                rewardStr = rewardStr.replace("n", String.valueOf(n)+" ");
                condition = new LawnMoverKillsCondition(n);
                reward = new CurrencyReward(true, n);
            }
            default -> {
                condition = new SunCollectCondition(100);
                reward = new CurrencyReward(false, 50);
            }
        }

        Quest quest = new Quest(title, category, priority, onMission, conditionStr);
        quest.setCondition(condition);
        quest.setReward(reward);

        return quest;
    }

    private static String extractString(JsonObject jsonObject, String key) {
        return jsonObject.has(key) && !jsonObject.get(key).isJsonNull() ? jsonObject.get(key).getAsString() : "";
    }

    private static int pickRandomFromDashSeparated(String text, int defaultValue) {
        if (text == null || text.trim().isEmpty() || text.equalsIgnoreCase("nan")) {
            return defaultValue;
        }

        try {
            String[] parts = text.split("-");
            int randomIndex = random.nextInt(parts.length);

            Matcher matcher = Pattern.compile("\\d+").matcher(parts[randomIndex]);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        } catch (Exception ignored) {
        }

        return defaultValue;
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