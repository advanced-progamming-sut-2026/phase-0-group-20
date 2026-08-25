package io.java.pvz.models.game.adventure.levels;

import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantCategory;
import io.java.pvz.models.entities.plants.PlantFactory;
import io.java.pvz.models.entities.zombies.zomboss.*;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.conditions.BossWinCondition;
import io.java.pvz.models.game.adventure.levels.conditions.NormalLoseCondition;
import io.java.pvz.models.game.adventure.levels.speciallevels.ConveyorBelt;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.timeManager.TimeManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BossLevel extends ConveyorBelt {

    private Zomboss zomboss;

    public BossLevel(String name, SeasonType season, int levelNumber) {
        super(name, season, 0, 0, levelNumber);

        this.winConditions.clear();
        this.loseConditions.clear();

        this.addLoseCondition(new NormalLoseCondition());
        setupDialogues();
    }

    @Override
    public void onLevelStart(GameSession session) {
        super.onLevelStart(session);

        int middleRow = session.getArena().getRows() / 2 - 1;

        zomboss = switch (season) {
            case ANCIENT_EGYPT -> new SpiderZomboss(middleRow);
            case FROZEN_CAVES -> new MammothZomboss(middleRow);
            case DARK_AGES -> new DragonZomboss(middleRow);
            case BIG_WAVE_BEACH -> new SharkZomboss(middleRow);
            default -> new SpiderZomboss(middleRow);
        };


        session.getArena().addZombie(zomboss);
        session.getTimeManager().registerNewTicker(zomboss);

        this.addWinCondition(new BossWinCondition(zomboss));

        notify("Dr. Zomboss has arrived! Defeat him to win!");
    }

    @Override
    public void engineLoop(GameSession session, int currentTick) {

        if (currentTick > 0 && currentTick % (6 * TimeManager.TICKS_PER_SECOND) == 0) {
            if (getBelt().size() < 10) {
                spawnPlantOnBelt();
            }
        }
    }

    @Override
    public float getDifficultyCoefficient() {
        return super.getDifficultyCoefficient() * 1.5f;
    }

    public Zomboss getZomboss() {
        return zomboss;
    }

    @Override
    public String toString() {
        return "Don't Let Zombies Eat Your Brain-Defeat The Zomboss";
    }

    @Override
    public void setupDialogues() {
        introDialogue.clear();
        switch (season) {
            case ANCIENT_EGYPT -> dialogueEgypt();
            case FROZEN_CAVES -> dialogueFrozen();
            case DARK_AGES -> dialogueDarkAges();
            case BIG_WAVE_BEACH -> dialogueBeach();
        }
    }

    private void dialogueEgypt() {
        addDialogueLine(
            "Crazy Dave",
            "Greetings, ancient neighbor! Look at that giant mechanical cat!",
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM",
            "anim_taco_talk",
            true
        );
        addDialogueLine(
            "Dr. Zomboss",
            "Foolish mortals! My Sphinx-inator shall crush your puny flora into pyramid dust!",
            "768/FULL/NPC/ZOMBOSS/ZOMBOSS.PAM",
            "zomboss_talk",
            false
        );
        addDialogueLine(
            "Crazy Dave",
            "Because I'm CRAAAAZY! Bring on the pyramid power!",
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM",
            "anim_taco_talk",
            true
        );
    }

    private void dialogueFrozen() {
        addDialogueLine(
            "Crazy Dave",
            "Brrrr! My taco is completely frozen solid!",
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM",
            "anim_taco_talk",
            true
        );
        addDialogueLine(
            "Dr. Zomboss",
            "Welcome to the Ice Age, Dave! Prepare to become a prehistoric frozen snack!",
            "768/FULL/NPC/ZOMBOSS/ZOMBOSS.PAM",
            "zomboss_talk",
            false
        );
        addDialogueLine(
            "Crazy Dave",
            "Not on my watch! Warm up the lawn, plants!",
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM",
            "anim_taco_talk",
            true
        );
    }

    private void dialogueDarkAges() {
        addDialogueLine(
            "Crazy Dave",
            "Hark, neighbor! Is that a metal dragon breathing fire?!",
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM",
            "anim_taco_talk",
            true
        );
        addDialogueLine(
            "Dr. Zomboss",
            "Kneel before the Dark Dragon! Your medieval defenses will burn to ash!",
            "768/FULL/NPC/ZOMBOSS/ZOMBOSS.PAM",
            "zomboss_talk",
            false
        );
        addDialogueLine(
            "Crazy Dave",
            "We have knights, mushrooms, and fire extinguishers! For the Kingdom!",
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM",
            "anim_taco_talk",
            true
        );
    }

    private void dialogueBeach() {
        addDialogueLine(
            "Crazy Dave",
            "Surf's up! But what is that giant metal shark lurking under the waves?",
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM",
            "anim_taco_talk",
            true
        );
        addDialogueLine(
            "Dr. Zomboss",
            "You are in deep waters now! The Sharktronic Sub will swallow your lawn whole!",
            "768/FULL/NPC/ZOMBOSS/ZOMBOSS.PAM",
            "zomboss_talk",
            false
        );
        addDialogueLine(
            "Crazy Dave",
            "Tangle Kelps and Lily Pads, assemble! Let's make a big splash!",
            "768/INITIAL/CRAZYDAVE/CRAZYDAVE/CRAZYDAVE.PAM",
            "anim_taco_talk",
            true
        );
    }

    @Override
    protected void spawnPlantOnBelt() {
        List<String> plantPool = new ArrayList<>(Arrays.asList(
            "Peashooter", "Wall-nut", "Cabbage-pult",
            "Snow Pea", "Repeater", "Snapdragon",
             "Bonk Choy", "Threepeater",
            "Melon-pult", "Kernel-pult", "Cactus",
            "Wasabi Whip", "Torchwood"
        ));

        switch (season) {
            case ANCIENT_EGYPT -> plantPool.addAll(Arrays.asList(
                "Iceberg Lettuce", "Grave Buster"
            ));
            case FROZEN_CAVES -> plantPool.addAll(Arrays.asList(
                "Rotobaga", "Hot Potato", "Winter Melon", "Split Pea"
            ));
            case DARK_AGES -> plantPool.addAll(Arrays.asList(
                "Pea Pod", "Doom-shroom", "Mega Gatling Pea", "Electric Blueberry"
            ));
            case BIG_WAVE_BEACH -> plantPool.addAll(Arrays.asList(
                "Lily Pad", "Tangle Kelp", "Bowling Bulb", "Sea-shroom", "Grapeshot"
            ));
        }

        Plant template;
        Plant newPlant = null;

        do {
            String randomPlantName = plantPool.get(random.nextInt(plantPool.size()));
            template = App.findPlantByName(randomPlantName);

            if (template != null &&
                template.getCategory() != PlantCategory.SUN_PRODUCER &&
                App.getActiveUser().isItUnlocked(template)) {
                newPlant = PlantFactory.create(template.getId());
            }
        } while (newPlant == null);

        getBelt().add(newPlant);
    }
}
