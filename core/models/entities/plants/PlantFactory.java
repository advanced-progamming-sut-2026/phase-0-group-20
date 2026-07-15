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

        boolean hasTrap = data.tags().contains(PlantTag.TRAP);
        boolean hasCharge = data.tags().contains(PlantTag.CHARGE);
        boolean isMint = nameKey.endsWith("-mint");

        boolean ownsItsOwnDetonation = nameKey.equals("grave buster")
                || nameKey.equals("ice-shroom")
                || nameKey.equals("hot potato");

        switch (data.category()) {
            case SUN_PRODUCER -> {
                if (!isMint) plant.addStrategy(new SunProductionStrategy());
            }

            case SHOOTER -> {
                if (!hasCharge && !isMint) plant.addStrategy(new ShootingStrategy());
            }

            case HOMING -> {
                if (!hasCharge && !nameKey.equals("magnet-shroom") && !isMint) {
                    plant.addStrategy(new HomingStrategy());
                }
            }

            case STRIKE_THROUGH -> {
                if (!hasCharge && !isMint) plant.addStrategy(new StrikeThroughStrategy());
            }

            case LOBBER -> {
                if (!isMint) plant.addStrategy(new LobberStrategy());
            }

            case EXPLOSIVE -> {
                if (!hasTrap && !ownsItsOwnDetonation && !isMint) plant.addStrategy(new ExplosiveStrategy());
            }

            case MELEE -> {
                if (!isMint && !nameKey.equals("chomper")) plant.addStrategy(new MeleeStrategy());
            }

            case WALL_NUT, MODIFIER -> {
                // null - passive defensive plants have no autonomous attack/produce loop
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
                    if (!hasTrap && !nameKey.equals("explode-o-nut")) plant.addStrategy(new ExplosiveStrategy());
                }
                case STACK, WATER, PEA, DAY, SHROOM, MAGIC -> {
                }
            }
        }

        if (isMint) {
            plant.addStrategy(new MintBuffStrategy());
        }

        switch (nameKey) {
            case "chomper":
                plant.addStrategy(new DigestionStrategy());
                break;

            case "magnet-shroom":
                plant.addStrategy(new MagnetStrategy());
                break;

            case "endurian":
                plant.addStrategy(new SpikeStrategy());
                break;

            case "tall-nut":
                plant.addStrategy(new AntiJumpStrategy());
                break;

            case "ice-shroom":
                plant.addStrategy(new GlobalEffectStrategy());
                break;

            case "grave buster":
                plant.addStrategy(new GraveBusterStrategy());
                break;

            case "doom-shroom":
                plant.addStrategy(new CraterStrategy());
                break;

            case "hot potato":
                plant.addStrategy(new MeltIceStrategy());
                break;

            case "torchwood":
                plant.addStrategy(new TorchwoodStrategy());
                break;

            case "lily pad":
                plant.addStrategy(new LilyPadStrategy());
                break;

            case "puff-shroom", "sea-shroom":
                plant.addStrategy(new LifespanStrategy(60));
                break;

            case "imitater": // copy a plant
                throw new UnsupportedOperationException("Imitater should not be instantiated on the board!");
        }

        wirePlantFoodStrategy(plant, data);

        return plant;
    }

    private static void wirePlantFoodStrategy(Plant plant, PlantData data) {
        switch (data.name().toLowerCase()) {

            case "sunflower" -> plant.addPlantFoodStrategy(new SunBurstFoodStrategy(150));
            case "twin sunflower" -> plant.addPlantFoodStrategy(new SunBurstFoodStrategy(250));
            case "sun-shroom" -> {
                plant.addPlantFoodStrategy(new SunBurstFoodStrategy(225));
                plant.addPlantFoodStrategy(new GrowToMaxSizeStrategy());
            }
            case "primal sunflower" -> plant.addPlantFoodStrategy(new SunBurstFoodStrategy(225));
            case "gold bloom", "enlighten-mint" -> plant.addPlantFoodStrategy(new NoFoodEffectStrategy());

            case "cat-tail" -> plant.addPlantFoodStrategy(new HomingRapidFireFoodStrategy());

            case "peashooter", "goo peashooter", "fire peashooter" ->
                    plant.addPlantFoodStrategy(new RapidFireFoodStrategy());
            case "repeater" -> plant.addPlantFoodStrategy(new RapidFireFoodStrategy(1, true));
            case "snow pea" -> plant.addPlantFoodStrategy(new IcyRapidFireFoodStrategy());
            case "split pea" -> plant.addPlantFoodStrategy(new BidirectionalRapidFireFoodStrategy());
            case "pea pod" -> plant.addPlantFoodStrategy(new RapidFireFoodStrategy(0, false));
            case "mega gatling pea" -> plant.addPlantFoodStrategy(new RapidFireFoodStrategy(4, true));

            case "threepeater" -> plant.addPlantFoodStrategy(new MultiLaneRapidFireFoodStrategy());
            case "rotobaga" -> plant.addPlantFoodStrategy(new MultiDirectionRapidFireFoodStrategy(4));
            case "starfruit" -> plant.addPlantFoodStrategy(new MultiDirectionRapidFireFoodStrategy(5));

            case "sea-shroom", "puff-shroom" -> {
                plant.addPlantFoodStrategy(new RapidFireFoodStrategy());
                plant.addPlantFoodStrategy(new ResetLifespanFoodStrategy());
            }

            case "citron" ->
                    plant.addPlantFoodStrategy(new LaneClearFoodStrategy("purifying plasma ball clears the whole lane"));
            case "cactus" ->
                    plant.addPlantFoodStrategy(new LaneClearFoodStrategy("electrified, high-damage, infinitely piercing spikes"));
            case "fume-shroom" ->
                    plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("a giant smoke cloud that pushes zombies back"));

            case "caulipower" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "hypnotized"));
            case "electric blueberry" ->
                    plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "instantly destroyed"));
            case "bowling bulb" ->
                    plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "giant exploding onion"));
            case "squash" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(2, "crushed"));
            case "tangle kelp" ->
                    plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "dragged underwater"));
            case "cabbage-pult" -> plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "cabbage lob"));
            case "melon-pult" ->
                    plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "giant watermelon lob"));
            case "winter melon" ->
                    plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "icy watermelon lob (slows)"));
            case "pepper-pult" ->
                    plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "large pepper lob (fire)"));
            case "chomper" ->
                    plant.addPlantFoodStrategy(new RandomTargetEffectFoodStrategy(3, "swallowed instantly from range"));

            case "garlic" ->
                    plant.addPlantFoodStrategy(new FieldWideEffectFoodStrategy("forces every zombie in the lane to move to another lane"));
            case "kernel-pult" ->
                    plant.addPlantFoodStrategy(new FieldWideEffectFoodStrategy("drops butter on every zombie on the field"));
            case "iceberg lettuce" ->
                    plant.addPlantFoodStrategy(new FieldWideEffectFoodStrategy("freezes every zombie currently visible"));
            case "sweet potato" ->
                    plant.addPlantFoodStrategy(new FieldWideEffectFoodStrategy("pulls in every nearby zombie and fully heals itself"));

            case "bonk choy" -> plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("rapid 3x3 punches"));
            case "phat beet" -> plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("powerful 3x3 sonic blast"));
            case "wasabi whip" ->
                    plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("spinning whip across a 3x3 area"));
            case "kiwibeast" ->
                    plant.addPlantFoodStrategy(new BurstEffectFoodStrategy("jumps and slams the ground for AoE damage"));

            case "potato mine", "primal potato mine" -> plant.addPlantFoodStrategy(new CloneAndArmFoodStrategy(2));

            case "wall-nut", "reinforce-mint" -> plant.addPlantFoodStrategy(new ArmorFoodStrategy(4000));
            case "tall-nut" -> plant.addPlantFoodStrategy(new ArmorFoodStrategy(8000));
            case "endurian" -> plant.addPlantFoodStrategy(new ArmorFoodStrategy(1000, true));
            case "explode-o-nut" -> plant.addPlantFoodStrategy(new ArmorFoodStrategy(1000));
            case "pumpkin" -> plant.addPlantFoodStrategy(new ArmorFoodStrategy(1000));
            case "sun bean" -> plant.addPlantFoodStrategy(new ArmorFoodStrategy(1000));

            case "torchwood" -> plant.addPlantFoodStrategy(new BlueFlameFoodStrategy());
            case "magnet-shroom" -> plant.addPlantFoodStrategy(new MultiMagnetFoodStrategy());
            case "hypno-shroom" -> plant.addPlantFoodStrategy(new GargantuarHypnotizeFoodStrategy());
            case "lily pad" -> plant.addPlantFoodStrategy(new DuplicateSelfFoodStrategy());

            case "cherry bomb", "grapeshot", "jalapeno", "doom-shroom", "ice-shroom",
                 "hot potato", "grave buster", "imitater", "pierce-mint", "enforce-mint" ->
                    plant.addPlantFoodStrategy(new NoFoodEffectStrategy());


            case "arma-mint", "bombard-mint", "enchant-mint", "appease-mint", "cattail-mint" ->
                    plant.addPlantFoodStrategy(new NoFoodEffectStrategy());

            default -> {
                System.out.println("WARNING: no PlantFoodStrategy mapped for '" + data.name()
                        + "' - falling back to NoFoodEffectStrategy. Check PlantFactory.wirePlantFoodStrategy().");
                plant.addPlantFoodStrategy(new NoFoodEffectStrategy());
            }
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