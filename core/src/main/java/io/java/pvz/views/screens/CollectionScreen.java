package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import io.java.pvz.controllers.GameController.CollectionController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.plants.PlantCategory;
import io.java.pvz.models.entities.plants.PlantTag;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.utils.ZombieCardButton;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class CollectionScreen extends BaseScreen {
    private final Skin skin;
    private boolean isShowingPlants = true;
    private final CollectionController controller = new CollectionController();
    private final List<PlantCardButton> allPlantCards = new ArrayList<>();

    private enum FilterState {
        ALL("Show All Plants"),
        UNLOCKED("Show Unlocked Plants"),
        UPGRADABLE("Show Upgradable Plants"),
        CATEGORY("Based on Categories");

        final String text;
        FilterState(String text) { this.text = text; }
    }

    private FilterState currentFilterState = FilterState.ALL;

    public CollectionScreen(Game game, Skin skin) {
        super(game);
        this.skin = skin;
        buildUI();
    }

    private void buildUI() {
        TextureBank textures = AssetLoader.getInstance().getTextures();

        TextButton toggleBtn = new TextButton("Zombies", skin);
        ImageButton closeBtn = createCloseButton(textures);
        Table topTable = createTopTable(toggleBtn, closeBtn);

        Table plantsTable = buildPlantsTable(textures);
        Table zombiesTable = buildZombiesTable(textures);

        ScrollPane scrollPane = new ScrollPane(plantsTable);
        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        Table sortBar = createSortBar(textures, plantsTable);

        Table barContainer = new Table();
        barContainer.bottom();
        barContainer.add(sortBar).fillX().size(600, 80).padBottom(30);
        barContainer.setVisible(isShowingPlants);

        Stack contentStack = new Stack();
        contentStack.add(scrollPane);
        contentStack.add(barContainer);

        Table bottomTable = new Table();
        bottomTable.setBackground(skin.getDrawable("image_ui_quests_panel_edge_to_edge_ten"));
        bottomTable.add(contentStack).expand().fill().pad(30);

        setupListeners(toggleBtn, closeBtn, scrollPane, plantsTable, zombiesTable, barContainer);

        mainLayer.add(topTable).growX().height(Value.percentHeight(0.1f, mainLayer)).row();
        mainLayer.add(bottomTable).grow().height(Value.percentHeight(0.9f, mainLayer));
    }

    private Table createTopTable(TextButton toggleBtn, ImageButton closeBtn) {
        Table topTable = new Table();
        topTable.add(toggleBtn).expand().bottom().left().padLeft(25).padBottom(0);
        topTable.add(closeBtn).expand().bottom().right().padRight(25).padBottom(0);
        return topTable;
    }

    private ImageButton createCloseButton(TextureBank textures) {
        Image closeClicked = UiFactory.imageFor(textures, "IMAGE_UI_ALMANAC_TABS_CLOSE_TAB_DOWN");
        Image closeUnClicked = UiFactory.imageFor(textures, "IMAGE_UI_ALMANAC_TABS_CLOSE_TAB");

        ImageButton closeBtn = new ImageButton(closeUnClicked.getDrawable(), closeClicked.getDrawable());
        closeBtn.setColor(Color.WHITE);
        return closeBtn;
    }

    private Table createSortBar(TextureBank textures, Table plantsTable) {
        Table sortBar = new Table();
        sortBar.setBackground(createSolidBackground(Color.valueOf("#F4F0DD")));
        sortBar.pad(12, 25, 12, 25);

        Label filterLabel = new Label(currentFilterState.text, skin);
        filterLabel.setColor(Color.valueOf("#2B7A0B"));
        filterLabel.setFontScale(1.3f);

        Table filterTable = createFilterTable(textures, plantsTable, filterLabel);
        Label collectionLabel = createCollectionLabel();

        sortBar.add(filterTable).left().expandX();
        sortBar.add(collectionLabel).right();
        return sortBar;
    }

    private Table createFilterTable(TextureBank textures, Table plantsTable, Label filterLabel) {
        Table filterTable = new Table();

        ImageButton filterButton = new ImageButton(
            UiFactory.imageFor(textures, Ids.PlantCards.FILTER_UNCLICKED).getDrawable(),
            UiFactory.imageFor(textures, Ids.PlantCards.FILTER_CLICKED).getDrawable()
        );

        filterButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cycleFilterState(filterLabel, plantsTable);
            }
        });

        filterTable.add(filterButton).padRight(8f);
        filterTable.add(filterLabel);
        filterTable.setTouchable(Touchable.enabled);
        return filterTable;
    }

    private void cycleFilterState(Label filterLabel, Table plantsTable) {
        FilterState[] states = FilterState.values();
        int nextIndex = (currentFilterState.ordinal() + 1) % states.length;
        currentFilterState = states[nextIndex];

        filterLabel.setText(currentFilterState.text);
        applyFilterToTable(plantsTable);
    }

    private Label createCollectionLabel() {
        int collected = App.getActiveUser().getUnlockedPlants().size();
        int total = App.getAllPlants().size();

        Label collectionLabel = new Label("Plants Collected: " + collected + " of " + total, skin, "medium");
        collectionLabel.setColor(Color.valueOf("#4A3018"));
        collectionLabel.setFontScale(1.1f);

        return collectionLabel;
    }

    private void setupListeners(TextButton toggleBtn, ImageButton closeBtn, ScrollPane scrollPane,
                                Table plantsTable, Table zombiesTable, Table barContainer) {
        toggleBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleAlmanacView(toggleBtn, scrollPane, plantsTable, zombiesTable, barContainer);
            }
        });

        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Closing Collection...");
                ScreenManager.getInstance().popScreen();
            }
        });
    }

    private void toggleAlmanacView(TextButton toggleBtn, ScrollPane scrollPane, Table plantsTable,
                                   Table zombiesTable, Table barContainer) {
        isShowingPlants = !isShowingPlants;

        if (isShowingPlants) {
            scrollPane.setActor(plantsTable);
            toggleBtn.setText("Zombies");
            barContainer.setVisible(true);
        } else {
            scrollPane.setActor(zombiesTable);
            toggleBtn.setText("Plants");
            barContainer.setVisible(false);
        }
    }

    private Drawable createSolidBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();

        Drawable drawable = new Image(new Texture(pixmap)).getDrawable();
        pixmap.dispose();

        return drawable;
    }

    private void applyFilterToTable(Table table) {
        table.clearChildren();
        int columns = 8;
        int count = 0;
        if(currentFilterState == FilterState.CATEGORY){
            allPlantCards.sort(Comparator.comparing(card -> card.getPlant().getCategory()));
            for(PlantCardButton card : allPlantCards){
                table.add(card).size(150, 115).expandX().padBottom(20);
                count++;

                if (count % columns == 0) {
                    table.row();
                }
            }
            return;
        }

        for (PlantCardButton card : allPlantCards) {
            boolean shouldShow = false;

            if (currentFilterState == FilterState.ALL) {
                shouldShow = true;
            } else if (currentFilterState == FilterState.UNLOCKED) {
                Plant plant = card.getPlant();
                shouldShow = App.getActiveUser().isItUnlocked(plant);
            } else if (currentFilterState == FilterState.UPGRADABLE) {
                shouldShow = card.isReadyToUpgrade();
            }

            if (shouldShow) {
                table.add(card).size(150, 115).expandX().padBottom(20);
                count++;

                if (count % columns == 0) {
                    table.row();
                }
            }
        }
    }

    private Table buildPlantsTable(TextureBank textures) {
        Table table = new Table();
        table.top();
        User currentUser = App.getActiveUser();
        if (allPlantCards.isEmpty()) {
            for (Plant plant : App.getAllPlants()) {
                if(currentUser.isItUnlocked(plant)){
                    Plant foundPlant = currentUser.getUnlockedPlants().stream()
                        .filter(p -> p.getName().equals(plant.getName()))
                        .findFirst()
                        .orElse(null);
                    if(foundPlant != null){
                        createPlantCard(textures,foundPlant);
                    }
                }else
                    createPlantCard(textures, plant);
            }
        }

        applyFilterToTable(table);
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
            Image zombieImage =(App.getActiveUser().isZombieUnlocked(zombie))?
                UiFactory.imageFor(textures, zombiePath):
                null;

            if (background != null) {
                ZombieCardButton card = new ZombieCardButton(background, zombieImage, zombie,skin);

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
                        System.out.println("Zombie clicked: " + zombie.getName());
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

    @Override
    public void show() {
        super.show();
        for (PlantCardButton card : allPlantCards) {
            card.updateState();
        }
    }

    private PlantCardButton createPlantCard(TextureBank textures, Plant plant) {
        String plantName = UiFactory.getAtlasName(plant);
        String plantTextureKey = "IMAGE_UI_PACKETS_" + plantName.toUpperCase();
        String familyTextureKey = getFamilyImageAddress(plant.getCategory());

        try {
            Image cardBg = UiFactory.imageFor(textures, getCardAddress(plant));
            Image plantImg = UiFactory.imageFor(textures, plantTextureKey);
            Image familyImg = UiFactory.imageFor(textures, familyTextureKey);

            if (plantImg == null || familyImg == null) {
                throw new NullPointerException("Image reference is null!");
            }

            PlantCardButton card = new PlantCardButton.Builder()
                .setBgImage(cardBg)
                .setPlantImage(plantImg)
                .setFamilyImage(familyImg)
                .setPlant(plant)
                .setSkin(skin)
                .build();

            allPlantCards.add(card);

            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    System.out.println("Plant clicked: " + plantName);
                    PlantInfoScreen infoScreen = new PlantInfoScreen(game, skin, plant, card, controller);
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
            case EXPLOSIVE ->  "IMAGE_UI_PACKETS_MINTFAM_EXPLOSIVE";
        };
    }
}
