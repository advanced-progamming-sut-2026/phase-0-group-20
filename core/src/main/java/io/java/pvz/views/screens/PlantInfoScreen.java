package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.strategy.category_strategy.SunProductionStrategy;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

public class PlantInfoScreen extends BaseScreen {
    private final Skin skin;
    private final Plant plant;
    private TextureRegion backgroundRegion;
    private final String atlasName;

    public PlantInfoScreen(Game game, Skin skin, Plant plant) {
        super(game);
        this.skin = skin;
        this.plant = plant;
        this.atlasName = UiFactory.getAtlasName(plant).toUpperCase();

        buildUI();
    }

    private void buildUI() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        backgroundRegion = textures.region(Ids.MainMenu.BACKGROUND);

        Table rootTable = new Table();
        rootTable.setFillParent(true);

        Label closeBtn = new Label("X", skin, "big");
        closeBtn.setColor(Color.WHITE);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.getInstance().popScreen();
            }
        });

        Table topBar = new Table();
        topBar.add(closeBtn).expandX().right().pad(20);
        rootTable.add(topBar).growX().row();

        Label titleLabel = new Label(plant.getName(), skin, "big");
        titleLabel.setAlignment(Align.center);
        rootTable.add(titleLabel).padBottom(20).row();

        Table contentTable = new Table();

        String pamPath1 = "768/INITIAL/PLANT/" + atlasName + "/" + atlasName + ".PAM";
        String pamPath2 = "768/FULL/PLANT/" + atlasName + "/" + atlasName + ".PAM";

        PamPlayer player = AssetLoader.getInstance().getPlayer();
        PamAnimatedActor plantActor = new PamAnimatedActor(player, "idle", pamPath1, pamPath2);

        contentTable.add(plantActor).size(200, 200).expand().bottom().padBottom(300).padLeft(350);

        BorderedTable statsTable = new BorderedTable();
        statsTable.top();

        float cellPadding = 35f;
        float blockWidth = 250f;

        Label informationLabel = new Label("Description", skin, "big");
        informationLabel.setColor(Color.valueOf("#4A3018"));
        informationLabel.setFontScale(1.8f);
        informationLabel.setAlignment(Align.center);

        statsTable.add(informationLabel).colspan(2).padTop(50).padBottom(cellPadding * 2).row();

        float padX = 35f;
        float padY = 15f;

        statsTable.add(createStatBlock(textures,
            "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_SUNCOST",
            "SUN COST", String.valueOf(plant.getCost()))).width(blockWidth).pad(padY, padX, padY, padX).left();

        statsTable.add(createStatBlock(textures,
            "IMAGE_UI_ALMANAC_PLANTS_RECHARGE_ICON",
            "RECHARGE", String.valueOf(plant.getRecharge()))).width(blockWidth).pad(padY, padX, padY, padX).left().row();

        statsTable.add(createStatBlock(textures,
            "IMAGE_UI_ALMANAC_PLANTS_TOUGHNESS_ICON",
            "TOUGHNESS", String.valueOf(plant.getBaseHp()))).width(blockWidth).pad(padY, padX, padY, padX).left();

        if(plant.getCategory() != PlantCategory.SUN_PRODUCER){
            statsTable.add(createStatBlock(textures,
                "IMAGE_UI_ALMANAC_PLANTS_DAMAGE_ICON",
                "DAMAGE", String.valueOf(plant.getDamage()))).width(blockWidth).pad(padY, padX, padY, padX).left().row();
        }else {
            SunProductionStrategy strategy = plant.getStrategies().stream()
                .filter(SunProductionStrategy.class::isInstance)
                .map(SunProductionStrategy.class::cast)
                .findFirst()
                .orElse(null);
            int amount = strategy.getSunTypeForPlant(plant.getName(),0).getValue();
            statsTable.add(createStatBlock(textures,
                "IMAGE_UI_ALMANAC_ALMANAC_STAT_ICON_SUNPRODUCTION",
                "SUN PRODUCTION", String.valueOf(amount))).width(blockWidth).pad(padY, padX, padY, padX).left().row();
        }

        statsTable.add(createStatBlock(textures,
            "IMAGE_UI_ALMANAC_PLANTS_RANGE_ICON",
            "RANGE", getRangeString(plant))).width(blockWidth).pad(padY, padX, padY, padX).left();

        statsTable.add(createStatBlock(textures,
            "IMAGE_UI_ALMANAC_ALMANAC_PIERCE",
            "Category", plant.getCategory().getName())).width(blockWidth).pad(padY, padX, padY, padX).left().row();
        contentTable.add(statsTable).size(850, 800).expand().top().right().padRight(50).padTop(20);

        rootTable.add(contentTable).grow();
        mainLayer.addActor(rootTable);
    }

    private String getRangeString(Plant plant){
        return switch (plant.getCategory()){
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

        Label titleLbl = new Label(title, skin,"big");
        titleLbl.setFontScale(1f
        );
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

    private static class PamAnimatedActor extends Actor {
        private final PamPlayer player;
        private final String clipName;
        private String successfulPath = null;
        private float stateTime = 0f;
        private boolean isLoaded = false;

        public PamAnimatedActor(PamPlayer player, String clipName, String... pamPaths) {
            this.player = player;
            this.clipName = clipName;

            for (String path : pamPaths) {
                try {
                    AssetLoader.getInstance().loadPamSync(path);
                    this.successfulPath = path;
                    this.isLoaded = true;
                    break;
                } catch (Exception e) {
                    System.err.println("⚠️ Fallback: Failed to load PAM from: " + path);
                }
            }

            if (!isLoaded) {
                System.err.println("❌ Critical Error: Could not load PAM animation from any of the provided paths.");
            }
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (isLoaded) {
                stateTime += delta;
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (isLoaded && player != null && successfulPath != null) {
                float drawX = getX() + (getWidth() / 2f);
                float drawY = getY();

                Matrix4 originalMatrix = batch.getTransformMatrix().cpy();

                Matrix4 scaledMatrix = originalMatrix.cpy()
                    .translate(drawX, drawY, 0)
                    .scale(1.5f, 1.5f, 1f)
                    .translate(-drawX, -drawY, 0);

                batch.setTransformMatrix(scaledMatrix);

                try {
                    player.draw(batch, successfulPath, clipName, stateTime, drawX, drawY, true);
                } catch (Exception e) {
                    System.err.println("❌ Rendering Error for PAM: " + successfulPath + " - " + e.getMessage());
                    isLoaded = false;
                } finally {
                    batch.setTransformMatrix(originalMatrix);
                }
            }
        }
    }
}
