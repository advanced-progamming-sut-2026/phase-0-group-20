package io.java.pvz.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.CollectionController;
import io.java.pvz.controllers.GameController.PlantSelectionController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.UiFactory;
import org.jspecify.annotations.NonNull;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

import java.util.*;

public class PlantSelectionModalTable extends Table {
    private final Skin skin;
    private Plant clickedPlant = null;
    private final PlantSelectionController controller;
    private final Runnable onComplete;
    private final BorderedTable topInfoTable;
    private final Map<Plant, PlantCardButton> cardMap = new HashMap<>();

    public PlantSelectionModalTable(Skin skin, Runnable onComplete) {
        super();
        this.skin = skin;
        this.onComplete = onComplete;
        this.controller = new PlantSelectionController();
        this.topInfoTable = new BorderedTable();

        setSize(900, 800);
        buildContent(skin);
    }

    private void buildContent(Skin skin) {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        top();
        List<Plant> availablePlants = getSortedPlants();
        if (availablePlants != null && !availablePlants.isEmpty()) {
            clickedPlant = availablePlants.getFirst();
        }

        Table gridTable = new Table();
        gridTable.top().padTop(10);
        int columns = 5;
        int count = 0;

        if (availablePlants != null) {
            for (Plant plant : availablePlants) {
                PlantCardButton card = createPlantCard(textures, plant);
                if (card != null) {
                    cardMap.put(plant, card);
                    gridTable.add(card).size(140, 105).pad(8);
                    count++;
                    if (count % columns == 0) {
                        gridTable.row();
                    }
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(gridTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        updateTopInfo(textures);

        TextButton startGameBtn = new TextButton("LET'S ROCK!", skin, "green");
        startGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if(controller.getSelectedPlants().isEmpty()) {
                    GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                        new GameEventPayload.Builder(GameEvent.NOTIFY)
                            .message("You need to choose at least one plant!")
                            .build());
                    return;
                }
                controller.startGame();
                remove();
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });

        add(topInfoTable).growX().height(250).pad(20).row();
        add(scrollPane).grow().pad(20).padTop(0).row();
        add(startGameBtn).size(250, 60).padBottom(20);
    }

    private static @NonNull List<Plant> getSortedPlants() {
        List<Plant> availablePlants = new ArrayList<>(App.getAllPlants());
        User activeUser = App.getActiveUser();

        availablePlants.sort((plant1, plant2) -> {
            boolean isUnlocked1 = activeUser.isItUnlocked(plant1);
            boolean isUnlocked2 = activeUser.isItUnlocked(plant2);

            return Boolean.compare(isUnlocked2, isUnlocked1);
        });
        return availablePlants;
    }

    private void updateTopInfo(TextureBank textures) {
        topInfoTable.clearChildren();
        if (clickedPlant == null) return;

        Table headerTable = new Table();

        Label titleLabel = new Label(clickedPlant.getName(), skin, "big");
        titleLabel.setAlignment(Align.center);
        titleLabel.setColor(Color.BROWN);

        headerTable.add(titleLabel).expandX().center();

        topInfoTable.add(headerTable).growX().colspan(2).padTop(30).row();

        Table animTable = new Table();
        Image animBg = UiFactory.imageFor(textures, "IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_CARNIVAL");
        if (animBg != null) animTable.setBackground(animBg.getDrawable());

        String atlasName = UiFactory.getAnimationName(clickedPlant);
        PamAnimatedActor animActor = PamAnimatedActor.createPlantIdle(atlasName);
        animActor.setScale(1.2f);
        animTable.add(animActor).size(100, 100).center().padBottom(60);

        topInfoTable.add(animTable).size(180, 180).left().padRight(20).padLeft(20).padBottom(40);

        Table rightContentTable = new Table();

        String desc = "Cost: " + clickedPlant.getCost() + " | Recharge: " + clickedPlant.getRecharge();
        Label descLabel = new Label(desc, skin, "medium");
        descLabel.setColor(Color.valueOf("#4A3018"));
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.topLeft);
        rightContentTable.add(descLabel).growX().height(60).top().row();

        if(App.getActiveUser().isItUnlocked(clickedPlant)) {
            Table buttonsTable = createButtonTable(textures);

            rightContentTable.add(buttonsTable).right().expandX().bottom();
        }

        topInfoTable.add(rightContentTable).grow().top();
    }

    private Table createButtonTable(TextureBank textures) {
        Table buttonsTable = new Table();
        PlantCardButton activeCard = cardMap.get(clickedPlant);

        if (activeCard != null && activeCard.isReadyToUpgrade()) {
            TextButton upgradeBtn = new TextButton("UPGRADE", skin, "purple");
            upgradeBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Result result = new CollectionController().upgradePlant(clickedPlant.getName());
                    if(result.isSuccessful()){
                        cardMap.get(clickedPlant).updateState();
                    }
                }
            });
            buttonsTable.add(upgradeBtn).size(130, 50).padRight(10);
        }

        TextButton boostBtn = generateBoostBtn(textures);

        buttonsTable.add(boostBtn).size(110, 50).padRight(10);
        buttonsTable.add(boostBtn).size(110, 50).padRight(10);

        boolean isSelected = activeCard != null && activeCard.getColor().equals(Color.DARK_GRAY);
        TextButton selectBtn = new TextButton(isSelected ? "DESELECT" : "SELECT", skin, "green");
        selectBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isSelected) {
                    Result res = controller.removePlant(clickedPlant.getName());
                    if (res != null && res.isSuccessful()) {
                        if (activeCard != null) {
                            activeCard.setColor(Color.WHITE);
                        }
                        updateTopInfo(textures);
                    }
                } else {
                    Result res = controller.addPlant(clickedPlant.getName());
                    if (res != null && res.isSuccessful()) {
                        if (activeCard != null) {
                            activeCard.setColor(Color.DARK_GRAY);
                        }
                        updateTopInfo(textures);
                    }
                }
            }
        });
        buttonsTable.add(selectBtn).size(110, 50);
        return buttonsTable;
    }

    private @NonNull TextButton generateBoostBtn(TextureBank textures) {
        List<String> boostedPlants = controller.getBoostedPlantNames();
        boolean isAlreadyBoosted = boostedPlants != null && boostedPlants.contains(clickedPlant.getName());
        TextButton boostBtn;
        if (isAlreadyBoosted) {
            boostBtn = new TextButton("BOOSTED", skin, "green");
            boostBtn.setDisabled(true);
            boostBtn.setTouchable(Touchable.disabled);
        } else {
            boostBtn = new TextButton("x2 BOOST", skin, "green");
            Image diamondIcon = UiFactory.imageFor(textures, "IMAGE_EFFECTS_COIN_DIAMOND_COIN_DIAMOND_141X146");

            if (diamondIcon != null) {
                boostBtn.add(diamondIcon).size(25, 25).padLeft(5);
            }

            boostBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Result result = controller.boostPlant(clickedPlant.getName());
                    if (result != null && result.isSuccessful()) {
                        PlantCardButton card = cardMap.get(clickedPlant);
                        if (card != null) {
                            card.setBoosted(true);
                        }
                        boostBtn.setDisabled(true);
                        boostBtn.setTouchable(Touchable.disabled);
                        boostBtn.setText("BOOSTED");
                    }
                }
            });
        }
        return boostBtn;
    }

    private PlantCardButton createPlantCard(TextureBank textures, Plant plant) {
        String plantName = UiFactory.getAtlasName(plant);
        String plantTextureKey = "IMAGE_UI_PACKETS_" + plantName.toUpperCase();
        String familyTextureKey = "IMAGE_UI_PACKETS_MINTFAM_MELEE";

        try {
            Image cardBg = UiFactory.imageFor(textures, getCardAddress(plant));
            Image plantImg = UiFactory.imageFor(textures, plantTextureKey);
            Image familyImg = UiFactory.imageFor(textures, familyTextureKey);

            if (plantImg == null || familyImg == null) {
                throw new NullPointerException("Image reference is null for " + plantName);
            }

            PlantCardButton card = new PlantCardButton.Builder()
                .setBgImage(cardBg)
                .setPlantImage(plantImg)
                .setFamilyImage(familyImg)
                .setPlant(plant)
                .setSkin(skin)
                .build();

            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (card.getColor().equals(Color.DARK_GRAY)) {
                        Result res = controller.removePlant(plant.getName());
                        if (res != null && res.isSuccessful()) {
                            card.setColor(Color.WHITE);
                            if (clickedPlant == plant) {
                                updateTopInfo(textures);
                            }
                        }
                    } else {
                        clickedPlant = plant;
                        updateTopInfo(textures);
                    }
                }
            });

            return card;

        } catch (Exception e) {
            System.err.println("Error Loading Plant Card for: " + plantName + " -> " + e.getMessage());
            return null;
        }
    }

    public void show(Group modalLayer, Viewport viewport) {
        setPosition(
            (viewport.getWorldWidth() - getWidth()) / 2f,
            (viewport.getWorldHeight() - getHeight()) / 2f
        );
        modalLayer.addActor(this);
    }

    private String getCardAddress(Plant plant) {
        List<PlantTag> tags = plant.getTags();
        if (tags.contains(PlantTag.ICE)) return "IMAGE_UI_PACKETS_ICEAGE";
        if (tags.contains(PlantTag.WATER)) return "IMAGE_UI_PACKETS_BEACH";
        if (tags.contains(PlantTag.EXPLOSIVE) || plant.getCategory() == PlantCategory.EXPLOSIVE)
            return "IMAGE_UI_PACKETS_DINO";
        if (tags.contains(PlantTag.MAGIC)) return "IMAGE_UI_PACKETS_EIGHTIES";
        if (tags.contains(PlantTag.NIGHT)) return "IMAGE_UI_PACKETS_DARK";
        if (tags.contains(PlantTag.CHARGE)) return "IMAGE_UI_PACKETS_FUTURE";
        if (tags.contains(PlantTag.TRAP)) return "IMAGE_UI_PACKETS_EGYPT";
        if (plant.getCategory() == PlantCategory.WALL_NUT) return "IMAGE_UI_PACKETS_COWBOY";
        if (plant.getCategory() == PlantCategory.SHOOTER) return "IMAGE_UI_PACKETS_LOSTCITY";
        if (plant.getCategory() == PlantCategory.SUN_PRODUCER) return "IMAGE_UI_PACKETS_BOOST";
        return "IMAGE_UI_PACKETS_HOMELESS";
    }
}
