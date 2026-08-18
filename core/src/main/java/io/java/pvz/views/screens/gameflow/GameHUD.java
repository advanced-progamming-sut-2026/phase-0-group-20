package io.java.pvz.views.screens.gameflow;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Wave;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.zomboss.Zomboss;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.adventure.levels.BossLevel;
import io.java.pvz.models.game.adventure.levels.speciallevels.ConveyorBelt;
import io.java.pvz.models.game.minigame.BeghouledLevel;
import io.java.pvz.models.game.minigame.IZombieLevel;
import io.java.pvz.models.game.minigame.VaseBreakerLevel;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.*;
import io.java.pvz.views.screens.modals.LevelIntroModalTable;
import io.java.pvz.views.screens.modals.PauseMenuTable;
import io.java.pvz.views.screens.modals.PlantSelectionModalTable;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.List;

public class GameHUD {
    private final Group mainLayer;
    private final Group modalLayer;
    private final Viewport viewport;
    private final GameInputHandler inputHandler;
    private final GameFlowController gameFlowController;
    private final Skin skin;
    private final TextureBank textures;

    private PlantFoodUI plantFoodBankUI;
    private ConveyorBeltUI belt;
    private ProgressBar waveProgressBar;
    private Image progressHeadIcon;
    private float visualWaveProgress = 0f;

    private Table seedBankTable;

    public GameHUD(Group mainLayer, Group modalLayer, Viewport viewport,
                   GameInputHandler inputHandler, GameFlowController gameFlowController) {
        this.mainLayer = mainLayer;
        this.modalLayer = modalLayer;
        this.viewport = viewport;
        this.inputHandler = inputHandler;
        this.gameFlowController = gameFlowController;
        this.skin = AssetLoader.getInstance().getSkin();
        this.textures = AssetLoader.getInstance().getTextures();
    }

    public void buildUI() {
        setupPlantSelectionMenu();
        setupIndicators();
        setupActionButtons();

        boolean isBossLevel = GameSession.getInstance() != null &&
            GameSession.getInstance().getCurrentMode() instanceof BossLevel;

        if (isBossLevel) {
            buildBossProgressBar();
        } else {
            buildNormalProgressBar();
        }
    }

    private void buildNormalProgressBar() {
        waveProgressBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "xp_green");
        waveProgressBar.setSize(400, 45);
        waveProgressBar.setPosition(1400f, 30f);
        waveProgressBar.setValue(0f);
        visualWaveProgress = 0f;
        mainLayer.addActor(waveProgressBar);

        if (GameSession.getInstance() != null && GameSession.getInstance().getCurrentMode() instanceof Level level) {
            int waveCount = level.getWaveCount();
            for (int i = 1; i <= waveCount; i++) {
                float fraction = (float) i / waveCount;
                Image flagImage = UiFactory.imageFor(textures,
                    "IMAGE_ZOMBIE_ZOMBIE_MODERN_VET_FLAG_ZOMBIE_MODERN_VET_FLAG_125X143");
                flagImage.setSize(40f, 45f);
                float x = waveProgressBar.getX() + (waveProgressBar.getWidth() * fraction)-(flagImage.getWidth() / 2f);
                float y = waveProgressBar.getY() + (waveProgressBar.getHeight() / 2f) - (flagImage.getHeight() / 2f);
                flagImage.setPosition(x, y);
                mainLayer.addActor(flagImage);
            }
        }

        progressHeadIcon = UiFactory.imageFor(textures, "IMAGE_UI_PERKS_RIFT_ICON_SAPMPLE_5");
        progressHeadIcon.setSize(55f, 55f);
        mainLayer.addActor(progressHeadIcon);
    }

    private void buildBossProgressBar() {
        waveProgressBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "xp_fuschia");
        waveProgressBar.setSize(450, 50);
        waveProgressBar.setPosition(1350f, 30f);
        waveProgressBar.setValue(1f);
        visualWaveProgress = 1f;
        mainLayer.addActor(waveProgressBar);

        for (int i = 1; i <= 2; i++) {
            float fraction = i / 3f;
            Image phaseMarker = UiFactory.imageFor(textures,
                "IMAGE_ZOMBIE_ZOMBIE_MODERN_VET_FLAG_ZOMBIE_MODERN_VET_FLAG_125X143");
            phaseMarker.setSize(35f, 40f);
            float x = waveProgressBar.getX() + (waveProgressBar.getWidth() * fraction) - (phaseMarker.getWidth() / 2f);
            float y = waveProgressBar.getY() + (waveProgressBar.getHeight() / 2f) - (phaseMarker.getHeight() / 2f);
            phaseMarker.setPosition(x, y);
            mainLayer.addActor(phaseMarker);
        }

        progressHeadIcon = UiFactory.imageFor(textures, "IMAGE_UI_HUD_INGAME_PROGRESS_METER_ZOMBOSS_HEAD");
        progressHeadIcon.setSize(65f, 65f);
        mainLayer.addActor(progressHeadIcon);
    }

    private void setupPlantSelectionMenu() {
        if(GameSession.getInstance() !=  null){
            GameSession.getInstance().pauseGame();
        }
        if (GameSession.getInstance() != null && GameSession.getInstance().getCurrentMode() instanceof BeghouledLevel) {
            buildBeghouledSeedBank();
            new LevelIntroModalTable(skin).show(modalLayer,viewport);

        } else if (GameSession.getInstance() != null &&
            GameSession.getInstance().getCurrentMode() instanceof IZombieLevel) {
            buildSeedBank();
            new LevelIntroModalTable(skin).show(modalLayer,viewport);
        } else if (App.getActiveMenu() == Menu.PLANTSELLECTION_MENU) {
            PlantSelectionModalTable plantSelectionModal = new PlantSelectionModalTable(skin, () -> {
                buildSeedBank();
                new LevelIntroModalTable(skin).show(modalLayer,viewport);
                GameSession.getInstance().pauseGame();

            });
            plantSelectionModal.show(modalLayer, viewport);
        } else if (GameSession.getInstance() != null &&
            !(GameSession.getInstance().getCurrentMode() instanceof VaseBreakerLevel)) {
            belt = new ConveyorBeltUI(skin, textures,
                (plant) -> createSeedPacket(plant, false));
            belt.setSize(200f, 700);
            belt.setPosition(20f, 250);
            mainLayer.addActor(belt);
            new LevelIntroModalTable(skin).show(modalLayer,viewport);
            GameSession.getInstance().pauseGame();
        }
    }

    public void refreshSeedBank() {
        if (GameSession.getInstance() != null && GameSession.getInstance().getCurrentMode() instanceof BeghouledLevel) {
            buildBeghouledSeedBank();
        } else {
            buildSeedBank();
        }
    }

    private void setupIndicators() {
        Stack sunStack = new Stack();
        sunStack.setSize(80, 80);
        sunStack.setPosition(100, 950);

        Image sunIcon = UiFactory.imageFor(textures, Ids.UI.SUN_ICON);
        sunStack.add(sunIcon);

        Label sunLabel = new Label("0", skin) {
            @Override
            public void act(float delta) {
                super.act(delta);
                if (GameSession.getInstance() != null) {
                    setText(String.valueOf(GameSession.getInstance().getCurrentSun()));
                }
            }
        };
        sunLabel.setAlignment(Align.center);
        sunLabel.setFontScale(1.2f);
        sunLabel.setColor(Color.BLACK);
        sunStack.add(sunLabel);

        sunStack.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                gameFlowController.cheatAddSun("50");
            }
        });

        mainLayer.addActor(sunStack);

        plantFoodBankUI = new PlantFoodUI(textures);
        plantFoodBankUI.setPosition(200f, 20f);

        plantFoodBankUI.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                inputHandler.onPlantFoodClicked();
            }
        });

        mainLayer.addActor(plantFoodBankUI);
        updatePlantFoodCount();
    }

    private void setupActionButtons() {
        if (App.getSettings().isDebug()) {
            Stack nukeBtn = UiFactory.iconButton(textures, skin, Ids.UI.NUKE_BUTTON, 50, 50, () -> {
                gameFlowController.releaseNuke();
            }, false);
            nukeBtn.setPosition(50, 50);
            mainLayer.addActor(nukeBtn);
        }

        Stack shovelBtn = UiFactory.iconButton(textures, skin, Ids.UI.SHOVEL, 110, 110, () -> {
            inputHandler.onShovelClicked();
        }, false);
        shovelBtn.setPosition(1350, 855);
        mainLayer.addActor(shovelBtn);

        Stack pauseBtn = UiFactory.iconButton(textures, skin, Ids.UI.PAUSE, 90, 90, () -> {
            if (GameSession.getInstance() != null) GameSession.getInstance().pauseGame();
            new PauseMenuTable(skin).show(modalLayer, viewport);
        });
        pauseBtn.setPosition(1730, 950);
        mainLayer.addActor(pauseBtn);

        Stack fastForwardBtn = UiFactory.iconButton(textures, skin, Ids.UI.FAST_FORWARD, 90, 90, () -> {
            GameSession session = GameSession.getInstance();
            if (session != null) {
                if (session.getSpeedMultiplier() == 1.0f) {
                    session.setSpeedMultiplier(2.0f);
                    GameSession.notify("⏩ Fast Forward: 2X SPEED");
                } else {
                    session.setSpeedMultiplier(1.0f);
                    GameSession.notify("▶️ Normal Speed");
                }
            }
        });
        fastForwardBtn.setPosition(1620, 950);
        mainLayer.addActor(fastForwardBtn);
    }

    private void buildSeedBank() {
        if (seedBankTable == null) {
            seedBankTable = new Table();
            seedBankTable.setPosition(20f, 100f);
            seedBankTable.setSize(180f, 800f);
            seedBankTable.top().left();
            seedBankTable.pad(20f);
            mainLayer.addActor(seedBankTable);
        } else {
            seedBankTable.clear();
        }

        List<Plant> selectedPlants = GameSession.getInstance().getChosenPlants();

        List<Zombie> selectedZombie = new ArrayList<>();
        if (GameSession.getInstance().getCurrentMode() instanceof IZombieLevel iZombieLevel)
            iZombieLevel.getZombiesForThisLevel()
                .forEach(s -> selectedZombie.add(InGameEntityGenerator.getZombieForGame(s, 0)));

        int maxSlots = 8;

        if (GameSession.getInstance().getCurrentMode() instanceof IZombieLevel) {
            for (int i = 0; i < maxSlots; i++) {
                if (i < selectedZombie.size()) {
                    ZombieCardButton zombieCardButton = createZombiePacket(selectedZombie.get(i));
                    seedBankTable.add(zombieCardButton).size(180f, 85f).padBottom(75).row();
                }
            }
        } else {
            for (int i = 0; i < maxSlots; i++) {
                if (i < selectedPlants.size()) {
                    PlantCardButton plantButton = createSeedPacket(selectedPlants.get(i), true);
                    seedBankTable.add(plantButton).size(180f, 85f).padBottom(10f).row();
                } else {
                    Table emptySlot = new Table();
                    Image emptySlotImg = UiFactory.imageFor(textures, "IMAGE_UI_PACKETS_EMPTY_PACKET");
                    emptySlot.setBackground(emptySlotImg.getDrawable());
                    seedBankTable.add(emptySlot).size(180f, 85f).padBottom(10f).row();
                }
            }
        }
    }

    private void buildBeghouledSeedBank() {
        if (seedBankTable == null) {
            seedBankTable = new Table();
            seedBankTable.setPosition(20f, 100f);
            seedBankTable.setSize(180f, 800f);
            seedBankTable.top().left();
            seedBankTable.pad(20f);
            mainLayer.addActor(seedBankTable);
        } else {
            seedBankTable.clear();
        }

        BeghouledLevel level = (BeghouledLevel) GameSession.getInstance().getCurrentMode();
        List<String> basePlants = level.getBasePlants();

        for (String plantName : basePlants) {
            Plant plant = App.findPlantByName(plantName);
            if (plant != null && level.isUpgradable(plant.getName())) {
                int cost = level.getUpgradeCost(plant.getName());
                PlantCardButton plantButton = createBeghouledUpgradePacket(plant, cost);
                seedBankTable.add(plantButton).size(180f, 85f).padBottom(10f).row();
            }
        }
    }

    private PlantCardButton createSeedPacket(Plant plant, boolean lockIncluded) {
        Image bgCard = UiFactory.imageFor(textures, Ids.PlantCards.BG_CARD);
        String plantName = UiFactory.getAtlasName(plant);
        String plantTextureKey = "IMAGE_UI_PACKETS_" + plantName.toUpperCase();
        Image plantIcon = UiFactory.imageFor(textures, plantTextureKey);

        PlantCardButton plantButton = new PlantCardButton.Builder()
            .setBgImage(bgCard)
            .setPlant(plant)
            .setPlantImage(plantIcon)
            .setSkin(skin)
            .setShowProgressBar(false)
            .setSize(90f)
            .setLockIncluded(lockIncluded)
            .setShowLevel(false)
            .build();

        if (belt == null) {
            Image cooldownOverlay = getCooldownEffect(plant, plantButton);
            plantButton.addActor(cooldownOverlay);
        }
        plantButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Float cd = GameSession.getInstance().getPlantsCooldown().get(plant);
                if (cd != null && cd > 0) {
                    GameSession.notify(plant.getName() + " is recharging!");
                    return;
                }

                inputHandler.onPlantCardClicked(plant, plantIcon);
            }
        });

        return plantButton;
    }

    private Image getCooldownEffect(Plant plant, PlantCardButton plantButton) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.4f);
        pixmap.fill();
        Texture darkOverlayTex = new Texture(pixmap);
        pixmap.dispose();

        Image cooldownOverlay = new Image(darkOverlayTex) {
            @Override
            public void act(float delta) {
                super.act(delta);
                if (GameSession.getInstance() == null) return;

                float cd = GameSession.getInstance().getPlantsCooldown().get(plant);
                if (cd > 0) {
                    this.setVisible(true);

                    float maxCd = plant.getRecharge() * TimeManager.TICKS_PER_SECOND;
                    float percentage = cd / maxCd;

                    this.setSize(plantButton.getWidth() * percentage, plantButton.getHeight());
                    this.setPosition(0, 0);
                } else {
                    this.setVisible(false);
                }
            }
        };
        cooldownOverlay.setTouchable(Touchable.disabled);
        return cooldownOverlay;
    }

    private PlantCardButton createBeghouledUpgradePacket(Plant plant, int cost) {
        Image bgCard = UiFactory.imageFor(textures, Ids.PlantCards.BG_CARD);
        String plantName = UiFactory.getAtlasName(plant);
        String plantTextureKey = "IMAGE_UI_PACKETS_" + plantName.toUpperCase();
        Image plantIcon = UiFactory.imageFor(textures, plantTextureKey);

        PlantCardButton plantButton = new PlantCardButton.Builder()
            .setBgImage(bgCard)
            .setPlant(plant)
            .setPlantImage(plantIcon)
            .setSkin(skin)
            .setShowProgressBar(false)
            .setSize(90f)
            .setCost(cost)
            .setLockIncluded(false)
            .setShowLevel(false)
            .build();

        plantButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                inputHandler.onBeghouledUpgradeClicked(plant.getName());
            }
        });

        return plantButton;
    }

    private ZombieCardButton createZombiePacket(Zombie zombie) {
        Image background = UiFactory.imageFor(textures, "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY");
        String zombiePath = "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_" + UiFactory.getZombieAddress(zombie);
        Image zombieImage = UiFactory.imageFor(textures, zombiePath);
        ZombieCardButton card = new ZombieCardButton(background, zombieImage, zombie,skin);
        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                inputHandler.onZombieCardClicked(zombie, zombieImage);
            }
        });
        return card;
    }

    public void update(float delta) {
        if (belt != null && GameSession.getInstance() != null &&
            GameSession.getInstance().getCurrentMode() instanceof ConveyorBelt beltLevel) {
            List<Plant> currentConveyorPlants = beltLevel.getBelt();
            belt.updateConveyor(delta, currentConveyorPlants);
        }
        calculateProgressBar(delta);
    }

    private void calculateProgressBar(float delta) {
        if (GameSession.getInstance() != null) {
            float targetProgress = 0f;

            if (GameSession.getInstance().getCurrentMode() instanceof BossLevel) {
                Zomboss zomboss = findZomboss();
                if (zomboss != null) {
                    float healthFraction = (float) Math.max(0, zomboss.getHealth()) / Math.max(1, zomboss.getBaseHp());
                    targetProgress = Math.max(0f, Math.min(1f, healthFraction));
                } else {
                    targetProgress = 1f;
                }
            } else if (GameSession.getInstance().getCurrentMode() instanceof Level level) {
                targetProgress = getNormalWaveProgress(level);
            }

            visualWaveProgress += (targetProgress - visualWaveProgress) * delta * 1.5f;

            if (waveProgressBar != null)
                waveProgressBar.setValue(visualWaveProgress);

            if (progressHeadIcon != null && waveProgressBar != null) {
                float targetX = waveProgressBar.getX() + (waveProgressBar.getWidth() *
                    visualWaveProgress) - (progressHeadIcon.getWidth() / 2f);
                float targetY = waveProgressBar.getY() +
                    (waveProgressBar.getHeight() / 2f) - (progressHeadIcon.getHeight() / 2f);
                progressHeadIcon.setPosition(targetX, targetY);
            }
        }
    }

    private Zomboss findZomboss() {
        if (GameSession.getInstance() != null && GameSession.getInstance().getArena() != null) {
            for (Zombie z : GameSession.getInstance().getArena().getActiveZombies()) {
                if (z instanceof Zomboss zomboss) {
                    return zomboss;
                }
            }
        }
        return null;
    }

    private float getNormalWaveProgress(Level level) {
        float targetProgress = 0f;
        int waveCount = level.getWaveCount();

        if (waveCount > 0) {
            float waveSlice = 1f / waveCount;
            float baseProgress = (level.getCurrentWave() - 1) * waveSlice;
            float currentWaveProgress = 0f;
            Wave activeWave = GameSession.getInstance().getArena().getCurrentActiveWave();

            if (activeWave != null && activeWave.getTotalBaseHp() > 0) {
                int currentHp = 0;
                for (Zombie z : activeWave.getZombies())
                    if (!z.isDead())
                        currentHp += z.getHealth();

                float destroyedFraction = 1f - ((float) currentHp / activeWave.getTotalBaseHp());

                if (activeWave.isLastWave())
                    currentWaveProgress = destroyedFraction;
                else
                    currentWaveProgress = Math.min(1f, destroyedFraction / 0.75f);
            }

            targetProgress = baseProgress + (currentWaveProgress * waveSlice);
            targetProgress = Math.max(0f, Math.min(1f, targetProgress));
        }
        return targetProgress;
    }

    public void updatePlantFoodCount() {
        if (plantFoodBankUI != null) {
            plantFoodBankUI.updateFood(App.getActiveUser().getPlantFoodCount());
        }
    }
}
