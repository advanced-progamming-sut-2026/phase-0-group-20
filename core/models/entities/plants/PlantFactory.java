package models.entities.plants;


import models.entities.plants.PlantFoodStrategy.*;
import models.entities.plants.strategy.*;
import models.entities.plants.strategy.category_strategy.*;
import models.entities.plants.strategy.tag_strategy.ChargeStrategy;
import models.entities.plants.strategy.tag_strategy.MoveZombiesStrategy;
import models.entities.plants.strategy.tag_strategy.SleepStrategy;
import models.entities.plants.strategy.tag_strategy.TrapStrategy;
import models.enums.plants.PlantTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlantFactory {

    private static Map<Integer, PlantData> plantRegistry = Map.of();

    public PlantFactory(Map<Integer, PlantData> plantReg) {
        plantRegistry = plantReg;
    }

    public static Plant create(int id) {
        PlantData data = plantRegistry.get(id);
        if (data == null) {
            throw new IllegalArgumentException(String.format("No plants with id %d", id));
        }

        Plant plant = new Plant(data);
        String nameKey = data.name().toLowerCase();

        if (nameKey.equals("imitater")) {
            throw new UnsupportedOperationException("Imitater should not be instantiated on the board!");
        }

        switch (data.abilityType()) {
            case "PRODUCE_SUN" -> plant.addStrategy(new SunProductionStrategy());

            case "SHOOT_PROJECTILE" -> {
                switch (data.category()) {
                    case HOMING -> {
                        if (!nameKey.equals("magnet-shroom")) plant.addStrategy(new HomingStrategy());
                    }
                    case LOBBER -> plant.addStrategy(new LobberStrategy());
                    case STRIKE_THROUGH -> plant.addStrategy(new StrikeThroughStrategy());
                    default -> {
                        if (!data.tags().contains(PlantTag.CHARGE)) plant.addStrategy(new ShootingStrategy());
                    }
                }
            }

            case "MELEE_ATTACK" -> {
                if (!nameKey.equals("chomper")) plant.addStrategy(new MeleeStrategy());
            }

            case "MINT_FAMILY_BOOST" -> plant.addStrategy(new MintBuffStrategy());

            case "DELAYED_EXPLOSIVE", "INSTANT_EXPLOSIVE" -> {
                if (!data.tags().contains(PlantTag.TRAP) && !ownsItsOwnDetonation(nameKey)) {
                    plant.addStrategy(new ExplosiveStrategy());
                }
            }
        }

        for (PlantTag tag : data.tags()) {
            switch (tag) {
                case SUN -> plant.addStrategy(new SunOnHitStrategy());
                case TRAP -> plant.addStrategy(new TrapStrategy());
                case CHARGE -> plant.addStrategy(new ChargeStrategy());
                case MOVE_ZOMBIES -> plant.addStrategy(new MoveZombiesStrategy());
                case NIGHT -> plant.addStrategy(new SleepStrategy());
                case EXPLOSIVE -> {
                    if (!data.tags().contains(PlantTag.TRAP) && !nameKey.equals("explode-o-nut"))
                        plant.addStrategy(new ExplosiveStrategy());
                }
                case STACK, WATER, PEA, DAY, SHROOM, MAGIC -> {
                }
            }
        }

        switch (nameKey) {
            case "chomper" -> plant.addStrategy(new DigestionStrategy());
            case "magnet-shroom" -> plant.addStrategy(new MagnetStrategy());
            case "endurian" -> plant.addStrategy(new SpikeStrategy());
            case "tall-nut" -> plant.addStrategy(new AntiJumpStrategy());
            case "ice-shroom" -> plant.addStrategy(new GlobalEffectStrategy());
            case "grave buster" -> plant.addStrategy(new GraveBusterStrategy());
            case "doom-shroom" -> plant.addStrategy(new CraterStrategy());
            case "hot potato" -> plant.addStrategy(new MeltIceStrategy());
            case "torchwood" -> plant.addStrategy(new TorchwoodStrategy());
            case "lily pad" -> plant.addStrategy(new LilyPadStrategy());
            case "puff-shroom", "sea-shroom" -> plant.addStrategy(new LifespanStrategy(60));
        }

        wirePlantFoodStrategy(plant, data);

        return plant;
    }

    private static boolean ownsItsOwnDetonation(String nameKey) {
        return nameKey.equals("grave buster") || nameKey.equals("ice-shroom") || nameKey.equals("hot potato");
    }



    private static void wirePlantFoodStrategy(Plant plant, PlantData data) {
        String foodType = data.plantFoodType();
        int foodValue = (int) data.plantFoodValue();
        String nameKey = data.name().toLowerCase();

        switch (foodType) {
            case "SPAWN_SUN_ITEMS" -> {
                plant.addPlantFoodStrategy(new SunBurstFoodStrategy(foodValue));
                if (nameKey.equals("sun-shroom")) plant.addPlantFoodStrategy(new GrowToMaxSizeStrategy());
            }
            case "GRANT_PERMANENT_ARMOR" -> plant.addPlantFoodStrategy(new ArmorFoodStrategy(foodValue));
            case "SPAWN_CLONES" -> plant.addPlantFoodStrategy(new CloneAndArmFoodStrategy(foodValue));
            case "MAP_WIDE_FREEZE" -> plant.addPlantFoodStrategy(new FieldWideEffectFoodStrategy("freezes every zombie currently visible"));
            case "PULL_UNDERWATER" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(foodValue, "dragged underwater"));
            case "RANDOM_HYPNOTIZE" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(foodValue, "hypnotized"));
            case "KNOCKBACK_BLAST" -> plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("a giant smoke cloud that pushes zombies back"));
            case "NONE" -> plant.addPlantFoodStrategy(new NoFoodEffectStrategy());

            case "PROJECTILE_BURST" -> {
                if (data.tags().contains(PlantTag.ICE)) {
                    plant.addPlantFoodStrategy(new IcyRapidFireFoodStrategy());
                } else if (nameKey.equals("repeater")) {
                    plant.addPlantFoodStrategy(new RapidFireFoodStrategy(1, true));
                } else if (nameKey.equals("split pea")) {
                    plant.addPlantFoodStrategy(new BidirectionalRapidFireFoodStrategy());
                } else if (nameKey.equals("pea pod")) {
                    plant.addPlantFoodStrategy(new RapidFireFoodStrategy(0, false));
                } else if (nameKey.equals("mega gatling pea")) {
                    plant.addPlantFoodStrategy(new RapidFireFoodStrategy(4, true));
                } else if (nameKey.equals("threepeater")) {
                    plant.addPlantFoodStrategy(new MultiLaneRapidFireFoodStrategy());
                } else if (nameKey.equals("rotobaga")) {
                    plant.addPlantFoodStrategy(new MultiDirectionRapidFireFoodStrategy(4));
                } else if (nameKey.equals("starfruit")) {
                    plant.addPlantFoodStrategy(new MultiDirectionRapidFireFoodStrategy(5));
                } else if (!data.category().equals("LOBBER") && !data.category().equals("HOMING")) {
                    plant.addPlantFoodStrategy(new RapidFireFoodStrategy());
                }

                if (data.tags().contains(PlantTag.SHROOM) && foodValue == 60.0) {
                    plant.addPlantFoodStrategy(new ResetLifespanFoodStrategy());
                }
            }
            default -> plant.addPlantFoodStrategy(new NoFoodEffectStrategy());
        }

        switch (nameKey) {
            case "citron" -> plant.addPlantFoodStrategy(new LaneClearFoodStrategy("purifying plasma ball clears the whole lane"));
            case "cactus" -> plant.addPlantFoodStrategy(new LaneClearFoodStrategy("electrified, high-damage, infinitely piercing spikes"));
            case "cabbage-pult" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "cabbage lob"));
            case "melon-pult" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "giant watermelon lob"));
            case "winter melon" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "icy watermelon lob (slows)"));
            case "pepper-pult" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "large pepper lob (fire)"));
            case "chomper" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "swallowed instantly from range"));
            case "garlic" -> plant.addPlantFoodStrategy(new FieldWideEffectFoodStrategy("forces every zombie in the lane to move to another lane"));
            case "kernel-pult" -> plant.addPlantFoodStrategy(new FieldWideEffectFoodStrategy("drops butter on every zombie on the field"));
            case "sweet potato" -> plant.addPlantFoodStrategy(new FieldWideEffectFoodStrategy("pulls in every nearby zombie and fully heals itself"));
            case "bonk choy" -> plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("rapid 3x3 punches"));
            case "phat beet" -> plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("powerful 3x3 sonic blast"));
            case "wasabi whip" -> plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("spinning whip across a 3x3 area"));
            case "kiwibeast" -> plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("jumps and slams the ground for AoE damage"));
            case "torchwood" -> plant.addPlantFoodStrategy(new BlueFlameFoodStrategy());
            case "magnet-shroom" -> plant.addPlantFoodStrategy(new MultiMagnetFoodStrategy());
            case "hypno-shroom" -> plant.addPlantFoodStrategy(new GargantuarHypnotizeFoodStrategy());
            case "lily pad" -> plant.addPlantFoodStrategy(new DuplicateSelfFoodStrategy());
        }
    }

    public static List<Plant> createListOfPlants(List<Plant> plants) {
        List<Plant> newList = new ArrayList<>();
        for (Plant plant : plants) {
            int id = plant.getId();
            Plant newPlant = create(id);
            newList.add(newPlant);
        }
        return newList;
    }
}