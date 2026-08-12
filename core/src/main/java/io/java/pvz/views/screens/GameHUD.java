package io.java.pvz.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Wave;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.adventure.levels.speciallevels.ConveyorBelt;
import io.java.pvz.models.game.minigame.BeghouledLevel;
import io.java.pvz.utils.ConveyorBeltUI;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.PlantFoodUI;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.views.screens.modals.PauseMenuTable;
import io.java.pvz.views.screens.modals.PlantSelectionModalTable;
import pvz.libpvz.textures.TextureBank;

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

    public GameHUD(Group mainLayer, Group modalLayer, Viewport viewport, GameInputHandler inputHandler, GameFlowController gameFlowController) {
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

        waveProgressBar = new ProgressBar(0f, 1f, 0.001f, false, skin, "xp_green");
        waveProgressBar.setSize(400, 45);
        waveProgressBar.setPosition(1400f, 30f);
        waveProgressBar.setValue(0f);
        mainLayer.addActor(waveProgressBar);

        setupWaveMarkers();

        progressHeadIcon = UiFactory.imageFor(textures, "IMAGE_UI_PERKS_RIFT_ICON_SAPMPLE_5");
        progressHeadIcon.setSize(55f, 55f);
        mainLayer.addActor(progressHeadIcon);
    }

    private void setupPlantSelectionMenu() {
        if (GameSession.getInstance() != null && GameSession.getInstance().getCurrentMode() instanceof BeghouledLevel) {
            buildBeghouledSeedBank();
        } else if (App.getActiveMenu() == Menu.PLANTSELLECTION_MENU) {
            PlantSelectionModalTable plantSelectionModal = new PlantSelectionModalTable(skin, () -> {
                buildSeedBank();
            });
            plantSelectionModal.show(modalLayer, viewport);
        } else {
            belt = new ConveyorBeltUI(skin, textures,
                (plant) -> createSeedPacket(plant,false));
            belt.setSize(200f, 700);
            belt.setPosition(20f, 250);
            mainLayer.addActor(belt);
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
        if(App.getSettings().isDebug()){
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
            new PauseMenuTable(skin).show(modalLayer, viewport);
        });
        pauseBtn.setPosition(1730, 950);
        mainLayer.addActor(pauseBtn);

        Stack fastForwardBtn = UiFactory.iconButton(textures, skin, Ids.UI.FAST_FORWARD, 90, 90, () -> {
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
        int maxSlots = 8;

        for (int i = 0; i < maxSlots; i++) {
            if (i < selectedPlants.size()) {
                PlantCardButton plantButton = createSeedPacket(selectedPlants.get(i) ,true);
                seedBankTable.add(plantButton).size(180f, 85f).padBottom(10f).row();
            } else {
                Table emptySlot = new Table();
                Image emptySlotImg = UiFactory.imageFor(textures, "IMAGE_UI_PACKETS_EMPTY_PACKET");
                emptySlot.setBackground(emptySlotImg.getDrawable());
                seedBankTable.add(emptySlot).size(180f, 85f).padBottom(10f).row();
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
            if (plant != null) {
                PlantCardButton plantButton = createBeghouledUpgradePacket(plant);
                seedBankTable.add(plantButton).size(180f, 85f).padBottom(10f).row();
            }
        }
    }

    private PlantCardButton createSeedPacket(Plant plant , boolean lockIncluded) {
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

        plantButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                inputHandler.onPlantCardClicked(plant, plantIcon);
            }
        });

        return plantButton;
    }

    private PlantCardButton createBeghouledUpgradePacket(Plant plant) {
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

    private void setupWaveMarkers() {
        if (waveProgressBar == null || GameSession.getInstance() == null) return;

        if (GameSession.getInstance().getCurrentMode() instanceof Level level) {
            int waveCount = level.getWaveCount();

            for (int i = 1; i <= waveCount; i++) {
                float fraction = (float) i / waveCount;
                Image flagImage = UiFactory.imageFor(textures,
                    "IMAGE_ZOMBIE_ZOMBIE_MODERN_VET_FLAG_ZOMBIE_MODERN_VET_FLAG_125X143");

                flagImage.setSize(40f, 45f);
                float x = waveProgressBar.getX() + (waveProgressBar.getWidth() * fraction) - (flagImage.getWidth() / 2f);
                float y = waveProgressBar.getY() + (waveProgressBar.getHeight() / 2f) - (flagImage.getHeight() / 2f);

                flagImage.setPosition(x, y);
                mainLayer.addActor(flagImage);
            }
        }
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
        if (GameSession.getInstance() != null && GameSession.getInstance().getCurrentMode() instanceof Level level) {
            float targetProgress = getTargetProgress(level);

            visualWaveProgress += (targetProgress - visualWaveProgress) * delta * 1.5f;

            if (waveProgressBar != null)
                waveProgressBar.setValue(visualWaveProgress);

            if (progressHeadIcon != null && waveProgressBar != null) {
                float targetX = waveProgressBar.getX() + (waveProgressBar.getWidth() *
                    visualWaveProgress) - (progressHeadIcon.getWidth() / 2f);
                float targetY = waveProgressBar.getY() + (waveProgressBar.getHeight() / 2f) - (progressHeadIcon.getHeight() / 2f);
                progressHeadIcon.setPosition(targetX, targetY);
            }
        }
    }

    private float getTargetProgress(Level level) {
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
