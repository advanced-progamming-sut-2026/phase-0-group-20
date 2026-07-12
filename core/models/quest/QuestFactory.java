package models.quest;

import models.quest.conditions.*;
import models.quest.reward.*;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestFactory {

    private static final Random random = new Random();

    public static IQuestCondition createCondition(int rowIndex, String title, String conditionStr, String variableStr) {

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
                String randomPlant = "Shooter";
                return new KillZombieCondition(10, randomPlant);
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
                return new SpeedKillCondition(10, 30);
            }
            case 7 -> {
                return new UseExplosivePlantsCondition(3);
            }
            case 8 -> {
                return new SymmetryLogicCondition(true);
            }
            case 9 -> {
                String familyType = "Appease-mint";
                return new OnlyFamilyKillCondition(familyType);
            }
            case 10 -> {
                String familyType = "Enforce-mint";
                return new NoFamilyUseCondition(familyType);
            }
            case 11 -> {
                return new DayWithNightPlantsCondition();
            }
            case 12 -> {
                return new WinStreakMaxDifficultyCondition(5);
            }
            case 13 -> {
                return new KillInFirstColumnNoMowerCondition(10);
            }
            case 14 -> {
                return new SymmetryLogicCondition(false);
            }
            case 15 -> {
                return new MaxSunProducersCondition(3);
            }
            case 16 -> {
                int colIndex = random.nextInt(9);
                return new EmptyColumnCondition(colIndex);
            }
            case 17 -> {
                int rowIndexToEmpty = random.nextInt(5);
                return new EmptyRowCondition(rowIndexToEmpty);
            }
            case 18 -> {
                int crossIndex = random.nextInt(5);
                return new EmptyCrossCondition(crossIndex);
            }
            case 19 -> {
                int n = pickRandomFromDashSeparated(variableStr, 10);
                return new LawnMowerKillCondition(n);
            }
        }
        return new DummyCondition();
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
            case 3 -> {
                return new CurrencyReward(true, 20);
            }
            case 4 -> {
                int n = pickRandomFromDashSeparated(variableStr, 2);
                return new SeedPackReward(null, Math.max(1, 20 - n));
            }
            case 5 -> {
                return new CurrencyReward(true, 200);
            }
            case 6 -> {
                return new CurrencyReward(false, 500);
            }
            case 7 -> {
                return new CurrencyReward(false, 100);
            }
            case 8 -> {
                return new CurrencyReward(false, 500);
            }
            case 9 -> {
                return new CurrencyReward(false, 1000);
            }
            case 10 -> {
                return new CurrencyReward(true, 100);
            }
            case 11 -> {
                return new CurrencyReward(true, 20);
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
            case 15 -> {
                return new CurrencyReward(true, 10);
            }
            case 16 -> {
                return new CurrencyReward(true, 10);
            }
            case 17 -> {
                return new CurrencyReward(true, 20);
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

        String englishNumbersText = text
                .replace("۰", "0").replace("۱", "1").replace("۲", "2")
                .replace("۳", "3").replace("۴", "4").replace("۵", "5")
                .replace("۶", "6").replace("۷", "7").replace("۸", "8").replace("۹", "9");

        try {
            String[] parts = englishNumbersText.split("-");
            int randomIndex = random.nextInt(parts.length);
            Matcher matcher = Pattern.compile("\\d+").matcher(parts[randomIndex]);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group());
            }
        } catch (Exception ignored) { }

        return defaultValue;
    }
}