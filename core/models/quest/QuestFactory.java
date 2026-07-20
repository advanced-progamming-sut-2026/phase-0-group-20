package models.quest;

import models.enums.plants.PlantCategory;
import models.enums.plants.PlantTag;
import models.quest.conditions.*;
import models.quest.reward.CurrencyReward;
import models.quest.reward.Reward;
import models.quest.reward.SeedPackReward;
import models.quest.reward.UnlockableReward;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestFactory {

    private static final Random random = new Random();

    public static QuestCondition createCondition(int rowIndex, String title, String conditionStr, String variableStr) {

        switch (rowIndex) {
            case 0 -> {
                int targetSun = pickRandomFromDashSeparated(variableStr, 3000);
                return new SunCollectCondition(targetSun);
            }
            case 1 -> {
                String chapter = "Random_Chapter";
                return new KillZombieCondition(50, chapter);
            }
            case 2 -> {
                String plantCategory = "Shooter";
                return new KillZombieCondition(10, plantCategory);
            }
            case 3 -> {
                return new KillZombieCondition(10, "Cactus");
            }
            case 4 -> {
                int n = pickRandomFromDashSeparated(variableStr, 2);
                return new MaxPlantLossCondition(n);
            }
            case 5 -> {
                return new CertainAmountOfSunCondition(0);
            }
            case 6 -> {
                return new TimeLimitCondition(30, 10);
            }
            case 7 -> {
                String plantCategory = "explosive";
                return new PlantCategoryUseCondition(plantCategory, 3);
            }
            case 8 -> {
                return new SymmetryLogicCondition(true);
            }
            case 9 -> {
                PlantCategory category = PlantCategory.getRandomPlantCategory();
                return new WinWithThatCategoryCondition(category, true);
            }
            case 10 -> {
                PlantCategory category = PlantCategory.getRandomPlantCategory();
                return new WinWithThatCategoryCondition(category, false);
            }
            case 11 -> {
                PlantTag plantTag = PlantTag.SHROOM;
                return new WinWithSpecificTagCondition(plantTag);
            }
            case 12 -> {
//                return new WinStreakMaxDifficultyCondition(5);
                //after difficulties
            }
            case 13 -> {
                return new KillWithNoLawnmowerCondition(10, 0);
            }
            case 14 -> {
                return new SymmetryLogicCondition(false);
            }
            case 15 -> {
                PlantCategory category = PlantCategory.SUN_PRODUCER;
                return new MaxPlantUsedCondition(category, 3);
            }
            case 16 -> {
                int colIndex = random.nextInt(9);
                return new EmptyLineCondition(-1, colIndex);
            }
            case 17 -> {
                int rowIndexToEmpty = random.nextInt(5);
                return new EmptyLineCondition(rowIndexToEmpty, -1);
            }
            case 18 -> {
                int crossIndex = random.nextInt(5);
                return new EmptyLineCondition(crossIndex, crossIndex);
            }
            case 19 -> {
                int n = pickRandomFromDashSeparated(variableStr, 10);
                return new LawnMoverKillsCondition(n);
            }
        }
        return null;
    }

    public static Reward createReward(int rowIndex, String rewardStr, String variableStr) {

        switch (rowIndex) {
            case 0 -> {
                int targetSun = pickRandomFromDashSeparated(variableStr, 3000);
                return new CurrencyReward(false, targetSun / 100);
            }
            case 1 -> {
                return new SeedPackReward(null, 10);
            }
            case 2 -> {
                return new UnlockableReward(null);
            }
            case 3, 11, 17 -> {
                return new CurrencyReward(true, 20);
            }
            case 4 -> {
                int n = pickRandomFromDashSeparated(variableStr, 2);
                return new SeedPackReward(null, Math.max(1, 20 - n));
            }
            case 5 -> {
                return new CurrencyReward(true, 200);
            }
            case 6, 8 -> {
                return new CurrencyReward(false, 500);
            }
            case 7 -> {
                return new CurrencyReward(false, 100);
            }
            case 9 -> {
                return new CurrencyReward(false, 1000);
            }
            case 10 -> {
                return new CurrencyReward(true, 100);
            }
            case 12 -> {
                return new CurrencyReward(false, 5000);
            }
            case 13 -> {
                return new CurrencyReward(false, 300);
            }
            case 14 -> {
                return new CurrencyReward(false, 800);
            }
            case 15, 16 -> {
                return new CurrencyReward(true, 10);
            }
            case 18 -> {
                return new CurrencyReward(true, 25);
            }
            case 19 -> {
                int n = pickRandomFromDashSeparated(variableStr, 10);
                return new CurrencyReward(true, n);
            }
        }
        return new CurrencyReward(false, 50);
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


}