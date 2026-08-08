package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.java.pvz.controllers.GameController.CollectionController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.enums.plants.PlantCategory;
import io.java.pvz.models.enums.plants.PlantTag;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.utils.ZombieCardButton;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

public class CollectionScreen extends BaseScreen {
    private final Skin skin;
    private boolean isShowingPlants = true;
    private final CollectionController controller = new CollectionController();
    public CollectionScreen(Game game, Skin skin) {
        super(game);
        this.skin = skin;
        buildUI();
    }

    private void buildUI() {
        TextureBank textures = AssetLoader.getInstance().getTextures();

        Table topTable = new Table();

        TextButton toggleBtn = new TextButton("Zombies", skin);

        Image closeClicked = UiFactory.imageFor(textures, "IMAGE_UI_ALMANAC_TABS_CLOSE_TAB_DOWN");
        Image closeUnClicked = UiFactory.imageFor(textures, "IMAGE_UI_ALMANAC_TABS_CLOSE_TAB");
        ImageButton closeBtn = new ImageButton(closeUnClicked.getDrawable(), closeClicked.getDrawable());
        closeBtn.setColor(Color.WHITE);

        topTable.add(toggleBtn).expand().bottom().left().padLeft(25).padBottom(0);
        topTable.add(closeBtn).expand().bottom().right().padRight(25).padBottom(0);

        Table bottomTable = new Table();
        bottomTable.setBackground(skin.getDrawable("image_ui_quests_panel_edge_to_edge_ten"));

        Table plantsTable = buildPlantsTable(textures);
        Table zombiesTable = buildZombiesTable(textures);

        ScrollPane scrollPane = new ScrollPane(plantsTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        toggleBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                isShowingPlants = !isShowingPlants;

                if (isShowingPlants) {
                    scrollPane.setActor(plantsTable);
                    toggleBtn.setText("Zombies");
                } else {
                    scrollPane.setActor(zombiesTable);
                    toggleBtn.setText("Plants");
                }
            }
        });

        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Closing Collection...");
                ScreenManager.getInstance().popScreen();
            }
        });

        bottomTable.add(scrollPane).expand().fill().pad(30);

        mainLayer.add(topTable).growX().height(Value.percentHeight(0.1f, mainLayer)).row();
        mainLayer.add(bottomTable).grow().height(Value.percentHeight(0.9f, mainLayer));
    }

    private Table buildPlantsTable(TextureBank textures) {
        Table table = new Table();
        table.top();

        int columns = 8;
        int count = 0;

        for (Plant plant : App.getAllPlants()) {
            PlantCardButton card = createPlantCard(textures, plant);

            if (card != null) {
                table.add(card).size(150, 115).expandX().padBottom(20);
                count++;

                if (count % columns == 0) {
                    table.row();
                }
            }
        }
        return table;
    }

    private Table buildZombiesTable(TextureBank textures) {
        Table table = new Table();

        table.top().padTop(30).padBottom(30);

        int columns = 6;
        int count = 0;

        for (Zombie zombie : App.getAllZombies()) {
            Image background = UiFactory.imageFor(textures, "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY");
            String zombiePath = "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_" + UiFactory.getZombieAddress(zombie);
            Image zombieImage = UiFactory.imageFor(textures, zombiePath);

            if (background != null && zombieImage != null) {
                ZombieCardButton card = new ZombieCardButton(background, zombieImage, zombie);

                table.add(card)
                    .size(card.getWidth(), card.getHeight())
                    .expandX()
                    .padBottom(25)
                    .padLeft(10)
                    .padRight(10);

                count++;

                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        System.out.println("Plant clicked: " + zombie.getName());

                        ZombieInfoScreen infoScreen = new ZombieInfoScreen(game, skin, zombie);
                        ScreenManager.getInstance().pushScreen(infoScreen);
                    }
                });

                if (count % columns == 0) {
                    table.row();
                }
            } else {
                System.err.println("❌ Texture missing for zombie: " + zombie.getName());
            }
        }

        return table;
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
                throw new NullPointerException("Image reference is null!");
            }

            PlantCardButton card = new PlantCardButton(cardBg, plantImg, familyImg, plant,skin);

            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    System.out.println("Plant clicked: " + plantName);

                    PlantInfoScreen infoScreen = new PlantInfoScreen(game, skin,
                        plant,card.isReadyToUpgrade(),controller);
                    ScreenManager.getInstance().pushScreen(infoScreen);
                }
            });

            return card;

        } catch (Exception e) {
            System.err.println("=========================================");
            System.err.println("❌ Error Loading Plant Card for: " + plantName);
            System.err.println("Attempted Plant Texture: " + plantTextureKey);
            System.err.println("Attempted Family Texture: " + familyTextureKey);
            System.err.println("Reason: " + e.getMessage());
            System.err.println("=========================================");

            return null;
        }
    }

    private String getCardAddress(Plant plant) {
        List<PlantTag> tags = plant.getTags();

        if (tags.contains(PlantTag.ICE)) {
            return "IMAGE_UI_PACKETS_ICEAGE";
        } else if (tags.contains(PlantTag.WATER)) {
            return "IMAGE_UI_PACKETS_BEACH";
        } else if (tags.contains(PlantTag.EXPLOSIVE) || plant.getCategory() == PlantCategory.EXPLOSIVE) {
            return "IMAGE_UI_PACKETS_DINO";
        } else if (tags.contains(PlantTag.MAGIC)) {
            return "IMAGE_UI_PACKETS_EIGHTIES";
        } else if (tags.contains(PlantTag.NIGHT)) {
            return "IMAGE_UI_PACKETS_DARK";
        } else if (tags.contains(PlantTag.CHARGE)) {
            return "IMAGE_UI_PACKETS_FUTURE";
        } else if (tags.contains(PlantTag.TRAP)) {
            return "IMAGE_UI_PACKETS_EGYPT";
        } else if (plant.getCategory() == PlantCategory.WALL_NUT) {
            return "IMAGE_UI_PACKETS_COWBOY";
        } else if (plant.getCategory() == PlantCategory.SHOOTER) {
            return "IMAGE_UI_PACKETS_LOSTCITY";
        } else if (plant.getCategory() == PlantCategory.SUN_PRODUCER) {
            return "IMAGE_UI_PACKETS_BOOST";
        } else {
            return "IMAGE_UI_PACKETS_HOMELESS";
        }
    }

    @Override
    public void render(float delta) {
        clearScreen(0f, 0f, 0f, 1f);

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }
}
