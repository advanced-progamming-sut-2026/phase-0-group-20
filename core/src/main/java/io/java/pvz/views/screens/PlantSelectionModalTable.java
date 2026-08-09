package io.java.pvz.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.GameController.PlantSelectionController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.pam.PamPlayer;
import pvz.skin.BorderedTable;

import java.util.ArrayList;
import java.util.List;

public class PlantSelectionModalTable extends BorderedTable {

    private final PlantSelectionController controller;
    private final Skin skin;
    private Table blocker;
    private final Runnable onGameStarted;

    private Table selectedPlantsTable;
    private Table collectionTable;
    private Label errorLabel;

    private final List<Plant> selectedPlantsLocal = new ArrayList<>();

    public PlantSelectionModalTable(Skin skin, Runnable onGameStarted) {
        super();
        this.skin = skin;
        this.controller = new PlantSelectionController();
        this.onGameStarted = onGameStarted;

        pad(30);
        setSize(1100, 850);

        buildContent();
    }

    private void buildContent() {
        selectedPlantsTable = new Table();
        collectionTable = new Table();

        errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.getColor().a = 0;

        updateUI();

        ScrollPane collectionScroll = new ScrollPane(collectionTable, skin);
        collectionScroll.setFadeScrollBars(false);
        collectionScroll.setScrollingDisabled(true, false);

        TextButton letsRockBtn = UiFactory.textButton("Let's Rock!", skin, "green", 1.05f, 0.95f, () -> {
            Result result = controller.startGame();
            if (result.isSuccessful()) {
                System.out.println(result.message());
                this.remove();
                if (onGameStarted != null) {
                    onGameStarted.run();
                }
            } else {
                showError(result.message());
            }
        });
        letsRockBtn.getLabel().setFontScale(1.4f);

        add(selectedPlantsTable).growX().minHeight(180).padBottom(10).row();
        add(collectionScroll).grow().padBottom(10).row();
        add(errorLabel).padBottom(10).row();
        add(letsRockBtn).size(280, 80).center();
    }

    private void updateUI() {
        selectedPlantsTable.clearChildren();
        collectionTable.clearChildren();

        Color brownColor = Color.valueOf("#4A3018");

        Label selectedTitle = new Label("Selected Plants", skin, "big");
        selectedTitle.setColor(brownColor);
        selectedPlantsTable.add(selectedTitle).colspan(8).padBottom(15).row();

        for (Plant p : selectedPlantsLocal) {
            selectedPlantsTable.add(createPlantCard(p, true)).pad(10);
        }

        Label collectionTitle = new Label("Your Collection", skin, "big");
        collectionTitle.setColor(brownColor);
        collectionTable.add(collectionTitle).colspan(7).padBottom(20).row();

        List<Plant> unlockedPlants = App.getActiveUser().getUnlockedPlants();
        if (unlockedPlants != null) {
            int count = 0;
            for (Plant p : unlockedPlants) {
                boolean isAlreadySelected = selectedPlantsLocal.stream()
                    .anyMatch(selected -> selected.getName().equals(p.getName()));

                if (!isAlreadySelected) {
                    collectionTable.add(createPlantCard(p, false)).pad(12);
                    count++;
                    if (count % 7 == 0) collectionTable.row();
                }
            }
        }
    }

    private Table createPlantCard(Plant plant, boolean isSelected) {
        Table card = new Table();

        String atlasName = UiFactory.getAtlasName(plant).toUpperCase();
        String pamPath1 = "768/INITIAL/PLANT/" + atlasName + "/" + atlasName + ".PAM";
        String pamPath2 = "768/FULL/PLANT/" + atlasName + "/" + atlasName + ".PAM";

        PamPlayer player = AssetLoader.getInstance().getPlayer();

        PamAnimatedActor pamActor = new PamAnimatedActor(player, "idle", 0.5f, pamPath1, pamPath2);

        card.add(pamActor).size(80, 80).padBottom(30).row();

        Label nameLbl = new Label(plant.getName(), skin);
        nameLbl.setColor(Color.valueOf("#4A3018"));
        nameLbl.setFontScale(0.85f);
        card.add(nameLbl).padBottom(5).row();

        Label costLbl = new Label(plant.getCost() + " Sun", skin, "medium_outline");
        costLbl.setColor(Color.YELLOW);
        costLbl.setFontScale(0.9f);
        card.add(costLbl);

        card.setTouchable(Touchable.enabled);
        ButtonAnimator.applyHoverAndClickEffect(card, 1.1f, 0.9f, () -> {
            if (isSelected) {
                Result res = controller.removePlant(plant.getName());
                if (res.isSuccessful()) {
                    selectedPlantsLocal.removeIf(p -> p.getName().equals(plant.getName()));
                    updateUI();
                } else {
                    showError(res.message());

                }
            } else {
                Result res = controller.addPlant(plant.getName());
                if (res.isSuccessful()) {
                    selectedPlantsLocal.add(plant);
                    updateUI();
                } else {
                    showError(res.message());
                }
            }
        });

        return card;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.clearActions();
        errorLabel.addAction(Actions.sequence(
            Actions.alpha(1f),
            Actions.delay(2f),
            Actions.fadeOut(1f)
        ));
    }

    public void show(Group targetLayer, Viewport viewport) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        blocker = new Table();
        blocker.setSize(width, height);
        blocker.setTouchable(Touchable.enabled);
        blocker.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        targetLayer.addActor(blocker);

        this.pack();
        this.setPosition(
            Math.round((width - this.getWidth()) / 2f),
            Math.round((height - this.getHeight()) / 2f)
        );

        targetLayer.addActor(this);
    }

    @Override
    public boolean remove() {
        if (blocker != null) blocker.remove();
        return super.remove();
    }

    private static class PamAnimatedActor extends Actor {
        private final PamPlayer player;
        private final String clipName;
        private final float scale;
        private String successfulPath = null;
        private float stateTime = 0f;
        private boolean isLoaded = false;

        public PamAnimatedActor(PamPlayer player, String clipName, float scale, String... pamPaths) {
            this.player = player;
            this.clipName = clipName;
            this.scale = scale;

            for (String path : pamPaths) {
                try {
                    AssetLoader.getInstance().loadPamSync(path);
                    this.successfulPath = path;
                    this.isLoaded = true;
                    break;
                } catch (Exception e) {
                }
            }
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (isLoaded) stateTime += delta;
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (isLoaded && player != null && successfulPath != null) {
                float drawX = getX() + (getWidth() / 2f);
                float drawY = getY();

                Matrix4 originalMatrix = batch.getTransformMatrix().cpy();

                Matrix4 scaledMatrix = originalMatrix.cpy()
                    .translate(drawX, drawY, 0)
                    .scale(scale, scale, 1f)
                    .translate(-drawX, -drawY, 0);

                batch.setTransformMatrix(scaledMatrix);

                try {
                    player.draw(batch, successfulPath, clipName, stateTime, drawX, drawY, true);
                } catch (Exception e) {
                    isLoaded = false;
                } finally {
                    batch.setTransformMatrix(originalMatrix);
                }
            }
        }
    }
}
