package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

import java.util.List;
import java.util.function.Consumer;

public class ImitaterSelectionModalTable extends Table {
    private final Skin skin;
    private final Consumer<Plant> onTargetSelected;
    private Actor blocker;

    public ImitaterSelectionModalTable(Skin skin, Consumer<Plant> onTargetSelected) {
        super();
        this.skin = skin;
        this.onTargetSelected = onTargetSelected;

        setSize(700, 600);
        buildContent();
    }

    private void buildContent() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        setBackground(skin.getDrawable("image_ui_if_bundle_reward1_bg_10"));
        top();

        Label titleLabel = new Label("Select a Plant to Imitate", skin, "big");
        titleLabel.setAlignment(Align.center);
        titleLabel.setColor(Color.BROWN);
        add(titleLabel).padTop(30).padBottom(20).row();

        Table gridTable = new Table();
        gridTable.top();
        int columns = 4;
        int count = 0;

        for (Plant plant : App.getActiveUser().getUnlockedPlants()) {
        if (plant.getName().equalsIgnoreCase("Imitater")) continue;

        PlantCardButton card = createPlantCard(textures, plant);
        if (card != null) {
            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (onTargetSelected != null) {
                        onTargetSelected.accept(plant);
                    }
                    remove();
                }
            });

            gridTable.add(card).size(130, 95).pad(8);
            count++;
            if (count % columns == 0) gridTable.row();
        }
    }

        ScrollPane scrollPane = new ScrollPane(gridTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        add(scrollPane).grow().pad(20).row();

        TextButton cancelBtn = new TextButton("CANCEL", skin, "green");
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove();
            }
        });
        add(cancelBtn).size(200, 60).padBottom(20);
    }

    private PlantCardButton createPlantCard(TextureBank textures, Plant plant) {
        try {
            String plantName = UiFactory.getAtlasName(plant);
            String plantTextureKey = "IMAGE_UI_PACKETS_" + plantName.toUpperCase();
            String familyTextureKey = getFamilyImageAddress(plant.getCategory());

            Image cardBg = UiFactory.imageFor(textures, getCardAddress(plant));
            Image plantImg = UiFactory.imageFor(textures, plantTextureKey);
            Image familyImg = UiFactory.imageFor(textures, familyTextureKey);

            if (plantImg == null || familyImg == null) return null;

            return new PlantCardButton.Builder()
                .setBgImage(cardBg)
                .setPlantImage(plantImg)
                .setFamilyImage(familyImg)
                .setPlant(plant)
                .setSkin(skin)
                .build();
        } catch (Exception e) {
            return null;
        }
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

    private String getFamilyImageAddress(PlantCategory category) {
        return switch (category) {
            case SUN_PRODUCER -> "IMAGE_UI_PACKETS_MINTFAM_SUN";
            case MELEE -> "IMAGE_UI_PACKETS_MINTFAM_MELEE";
            case STRIKE_THROUGH -> "IMAGE_UI_PACKETS_MINTFAM_ELECTRICITY";
            case HOMING -> "IMAGE_UI_PACKETS_MINTFAM_SHADOW";
            case LOBBER -> "IMAGE_UI_PACKETS_MINTFAM_LOBBER";
            case SHOOTER -> "IMAGE_UI_PACKETS_MINTFAM_PEASHOOTER";
            case MODIFIER -> "IMAGE_UI_PACKETS_MINTFAM_MAGIC";
            case WALL_NUT -> "IMAGE_UI_PACKETS_MINTFAM_DEFENSE";
            case EXPLOSIVE -> "IMAGE_UI_PACKETS_MINTFAM_EXPLOSIVE";
        };
    }

    public void show(Group modalLayer, Viewport viewport) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        Table blockerTable = new Table();
        blockerTable.setSize(width, height);
        blockerTable.setTouchable(Touchable.enabled);
        blockerTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        this.blocker = blockerTable;
        modalLayer.addActor(blocker);
        this.setPosition(
            Math.round((width - this.getWidth()) / 2f),
            Math.round((height - this.getHeight()) / 2f)
        );
        modalLayer.addActor(this);
    }

    @Override
    public boolean remove() {
        if (blocker != null) blocker.remove();
        return super.remove();
    }
}
