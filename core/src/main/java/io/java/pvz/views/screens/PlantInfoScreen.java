package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import io.java.pvz.controllers.GameController.CollectionController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.category_strategy.SunProductionStrategy;
import io.java.pvz.models.entities.plants.PlantCategory;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.utils.PamAnimatedActor;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

public class PlantInfoScreen extends BaseScreen {
    private static final int BASE_SEED_PACKETS = 10;
    private final Skin skin;
    private final Plant plant;
    private TextureRegion backgroundRegion;
    private final String atlasName;
    private final PlantCardButton cardButton;
    private final CollectionController controller;


    public PlantInfoScreen(Game game, Skin skin, Plant plant, PlantCardButton card, CollectionController controller) {
        super(game);
        this.skin = skin;
        this.plant = plant;
        this.atlasName = UiFactory.getAnimationName(plant).toUpperCase();
        this.cardButton = card;
        this.controller = controller;

        buildUI();
    }

    private void buildUI() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);

        Table rootTable = new Table();
        rootTable.setFillParent(true);

        rootTable.add(buildTopBar()).growX().row();

        rootTable.add(buildTitleLabel()).padBottom(20).row();

        Table contentTable = new Table();

        Table previewTable = buildPlantPreviewTable(textures);
        contentTable.add(previewTable).size(450, 550).expand().center().padLeft(50);

        Table statsTable = buildStatsTable(textures);
        contentTable.add(statsTable).size(850, 800).expand().top().right().padRight(50).padTop(20);

        rootTable.add(contentTable).grow();
        mainLayer.addActor(rootTable);
    }

    private Table buildTopBar() {
        Table topBar = new Table();
        Label closeBtn = new Label("X", skin, "big");
        closeBtn.setColor(Color.WHITE);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.getInstance().popScreen();
            }
        });

        topBar.add(closeBtn).expandX().right().pad(20);
        return topBar;
    }

    private Label buildTitleLabel() {
        Label titleLabel = new Label(plant.getName(), skin, "big");
        titleLabel.setAlignment(Align.center);
        return titleLabel;
    }

    private Table buildPlantPreviewTable(TextureBank textures) {
        Table previewTable = new Table();

        Image bground = UiFactory.imageFor(textures, "IMAGE_BACKGROUNDS_FRONTLAWN_ROW_05");
        if (bground != null) {
            previewTable.setBackground(bground.getDrawable());
        }

        PamAnimatedActor plantActor = PamAnimatedActor.createPlantIdle(atlasName);
        plantActor.setScale(1.5f);
        previewTable.add(plantActor).size(200, 200).expand().center().row();
        Stack progressStack = new Stack();

        ProgressBar progressBar = new ProgressBar(0, 10, 1, false, skin, "xp_yellow") {
            @Override
            public void act(float delta) {
                super.act(delta);
                int mathPower = (int) Math.pow(2, plant.getLevel());
                int maxCost = BASE_SEED_PACKETS * mathPower;
                setRange(0, maxCost);

                int currentAmount = App.getActiveUser().getInventory().getSeedPackets()
                    .getOrDefault(plant.getName(), 0);
                setValue(currentAmount);
            }
        };

        Label progressLabel = new Label("0/0", skin) {
            @Override
            public void act(float delta) {
                super.act(delta);
                int mathPower = (int) Math.pow(2, plant.getLevel());
                int maxCost = BASE_SEED_PACKETS * mathPower;

                int currentAmount = App.getActiveUser().getInventory().getSeedPackets()
                    .getOrDefault(plant.getName(), 0);
                setText(currentAmount + "/" + maxCost);
            }
        };
        progressLabel.setAlignment(Align.center);
        progressLabel.setFontScale(0.8f);

        progressStack.add(progressBar);
        progressStack.add(progressLabel);


        previewTable.add(progressStack).fillX().height(25).bottom().padBottom(10);

        return previewTable;
    }


    private Table buildStatsTable(TextureBank textures) {
        BorderedTable statsTable = new BorderedTable();
        statsTable.top();

        float cellPadding = 35f;
        float blockWidth = 250f;
        float padX = 35f;
        float padY = 15f;

        Label informationLabel = new Label("Description", skin, "big");
        informationLabel.setColor(Color.valueOf("#4A3018"));
        informationLabel.setFontScale(1.8f);
        informationLabel.setAlignment(Align.center);
        statsTable.add(informationLabel).colspan(2).padTop(50).padBottom(cellPadding * 2).row();
        statsTable.add(createStatBlock(textures, "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_SUNCOST",
                "SUN COST", String.valueOf(plant.getCost())))
            .width(blockWidth).pad(padY, padX, padY, padX).left();
        statsTable.add(createStatBlock(textures, "IMAGE_UI_ALMANAC_PLANTS_RECHARGE_ICON",
                "RECHARGE", String.valueOf(plant.getRecharge())))
            .width(blockWidth).pad(padY, padX, padY, padX).left().row();
        statsTable.add(createStatBlock(textures, "IMAGE_UI_ALMANAC_PLANTS_TOUGHNESS_ICON",
                "TOUGHNESS", String.valueOf(plant.getBaseHp())))
            .width(blockWidth).pad(padY, padX, padY, padX).left();

        if (plant.getCategory() != PlantCategory.SUN_PRODUCER) {
            statsTable.add(createStatBlock(textures, "IMAGE_UI_ALMANAC_PLANTS_DAMAGE_ICON",
                    "DAMAGE", String.valueOf(plant.getDamage())))
                .width(blockWidth).pad(padY, padX, padY, padX).left().row();
        } else {
            SunProductionStrategy strategy = plant.getStrategies().stream()
                .filter(SunProductionStrategy.class::isInstance)
                .map(SunProductionStrategy.class::cast)
                .findFirst()
                .orElse(null);
            int amount = strategy != null ? strategy.getSunTypeForPlant(plant.getName(), 0, plant).getValue() : 0;
            statsTable.add(createStatBlock(textures, "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_SUNPRODUCTION",
                    "SUN PRODUCTION", String.valueOf(amount)))
                .width(blockWidth).pad(padY, padX, padY, padX).left().row();
        }
        statsTable.add(createStatBlock(textures, "IMAGE_UI_ALMANAC_PLANTS_RANGE_ICON", "RANGE", getRangeString(plant)))
            .width(blockWidth).pad(padY, padX, padY, padX).left();
        statsTable.add(createStatBlock(textures, "IMAGE_UI_ALMANAC_ALMANAC_PIERCE",
                "Category", plant.getCategory().getName()))
            .width(blockWidth).pad(padY, padX, padY, padX).left().row();
        buildActionButtons(statsTable);

        return statsTable;
    }

    private void buildActionButtons(Table statsTable) {
        User user = App.getActiveUser();

        if (cardButton.isReadyToUpgrade() && user.isItUnlocked(plant) && plant.getLevel() < controller.getMaxLevel()) {
            TextButton upgradeButton = new TextButton("Upgrade", skin, "purple");
            upgradeButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    controller.upgradePlant(plant.getName());
                }
            });
            statsTable.add(upgradeButton).colspan(2).padTop(30).row();
        }

        if (!user.isItUnlocked(plant)) {
            TextButton buyButton = new TextButton("BUY", skin, "purple");
            buyButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    controller.purchasePlant(plant.getName());
                }
            });
            statsTable.add(buyButton).colspan(2).padTop(30).row();
        }
    }

    private String getRangeString(Plant plant) {
        return switch (plant.getCategory()) {
            case LOBBER -> "LOBBED";
            case SHOOTER -> "SHOT";
            case MELEE -> "CLOSE";
            case STRIKE_THROUGH -> "PIERCING";
            default -> "NONE";
        };
    }

    private Table createStatBlock(TextureBank textures, String iconKey, String title, String value) {
        Table block = new Table();
        block.left();

        Image icon = UiFactory.imageFor(textures, iconKey);
        if (icon != null) {
            block.add(icon).size(110, 110).padRight(15).left();
        }

        Table textTable = new Table();
        textTable.left();

        Label titleLbl = new Label(title, skin, "big");
        titleLbl.setFontScale(1f);
        titleLbl.setColor(Color.valueOf("#4A3018"));

        Label valueLbl = new Label(value, skin, "medium_outline");
        valueLbl.setFontScale(1.2f);
        valueLbl.setColor(Color.WHITE);

        textTable.add(titleLbl).left().row();
        textTable.add(valueLbl).left();

        block.add(textTable).expandX().left();

        return block;
    }

    @Override
    public void render(float delta) {
        clearScreen(0f, 0f, 0f, 1f);
        AssetLoader.getInstance().updateTextures();

        if (backgroundRegion != null) {
            batch.begin();
            batch.draw(backgroundRegion, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();
        }

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }
}
