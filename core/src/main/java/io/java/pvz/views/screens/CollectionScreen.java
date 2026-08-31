package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import io.java.pvz.controllers.GameController.CollectionController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.plants.PlantCategory;
import io.java.pvz.models.entities.plants.PlantTag;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.utils.ZombieCardButton;
import io.java.pvz.views.screens.BaseScreen;
import io.java.pvz.views.screens.PlantInfoScreen;
import io.java.pvz.views.screens.ZombieInfoScreen;
import pvz.libpvz.textures.TextureBank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionScreen extends BaseScreen {
    private final Skin skin;
    private boolean isShowingPlants = true;
    private boolean isFilterMenuVisible = false;
    private final CollectionController controller = new CollectionController();
    private final List<PlantCardButton> allPlantCards = new ArrayList<>();
    private final Set<PlantCategory> selectedCategories = new HashSet<>(Arrays.asList(PlantCategory.values()));
    private final List<CheckBox> categoryCheckBoxes = new ArrayList<>();
    private Table barContainer;

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

        TextButton toggleBtn = new TextButton("Zombies", skin,"green");
        TextButton filterMenuBtn = new TextButton("Filter", skin,"green");
        ImageButton closeBtn = createCloseButton(textures);
        Table topTable = createTopTable(toggleBtn, filterMenuBtn, closeBtn);

        Table plantsTable = buildPlantsTable(textures);
        Table zombiesTable = buildZombiesTable(textures);

        ScrollPane scrollPane = new ScrollPane(plantsTable, skin) {
            @Override
            protected void setStage(Stage stage) {
                super.setStage(stage);
                if (stage != null) stage.setScrollFocus(this);
            }
        };

        scrollPane.setScrollingDisabled(true, false);
        scrollPane.setFadeScrollBars(false);

        Table sortBar = createSortBar(textures, plantsTable);
        Table categorySelectionTable = buildCategorySelectionTable(plantsTable);

        barContainer = new Table();
        barContainer.setBackground(createSolidBackground(Color.valueOf("#F4F0DD")));
        barContainer.add(sortBar).fillX().size(800, 80).row();
        barContainer.add(categorySelectionTable).fillX().padBottom(15);
        barContainer.pack();

        Stack contentStack = new Stack();
        contentStack.add(scrollPane);

        Table bottomTable = new Table();
        bottomTable.setBackground(skin.getDrawable("image_ui_quests_panel_edge_to_edge_ten"));
        bottomTable.add(contentStack).expand().fill().pad(30);

        setupListeners(toggleBtn, filterMenuBtn, closeBtn, scrollPane, plantsTable, zombiesTable);

        mainLayer.add(topTable).growX().height(Value.percentHeight(0.1f, mainLayer)).row();
        mainLayer.add(bottomTable).grow().height(Value.percentHeight(0.9f, mainLayer));

        mainLayer.addActor(barContainer);
        barContainer.setPosition(viewport.getWorldWidth() / 2f - barContainer.getWidth() / 2f,
            -barContainer.getHeight() - 50f);
    }

    private Table buildCategorySelectionTable(Table plantsTable) {
        Table table = new Table();
        table.top().left();
        table.pad(10, 25, 20, 25);

        int cols = 4;
        int count = 0;
        boolean isCategoryState = currentFilterState == FilterState.CATEGORY;

        for (PlantCategory category : PlantCategory.values()) {
            CheckBox checkBox = new CheckBox(category.name(), skin);
            checkBox.setChecked(true);
            checkBox.getLabel().setColor(Color.valueOf("#4A3018"));
            checkBox.getLabel().setFontScale(1.1f);

            checkBox.setDisabled(!isCategoryState);
            checkBox.getColor().a = isCategoryState ? 1f : 0.5f;

            checkBox.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (checkBox.isDisabled()) return;
                    if (checkBox.isChecked()) {
                        selectedCategories.add(category);
                    } else {
                        selectedCategories.remove(category);
                    }
                    applyFilterToTable(plantsTable);
                }
            });

            categoryCheckBoxes.add(checkBox);
            table.add(checkBox).pad(5).left().expandX();
            count++;

            if (count % cols == 0) table.row();
        }
        return table;
    }

    private Table createTopTable(TextButton toggleBtn, TextButton filterMenuBtn, ImageButton closeBtn) {
        Table topTable = new Table();
        Table leftGroup = new Table();
        leftGroup.add(toggleBtn).padRight(15);
        leftGroup.add(filterMenuBtn);

        topTable.add(leftGroup).expand().bottom().left().padLeft(25).padBottom(0);
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

        boolean isCategoryState = currentFilterState == FilterState.CATEGORY;
        for (CheckBox cb : categoryCheckBoxes) {
            cb.setDisabled(!isCategoryState);
            cb.getColor().a = isCategoryState ? 1f : 0.5f;
        }

        applyFilterToTable(plantsTable);
    }

    private void toggleFilterMenu() {
        isFilterMenuVisible = !isFilterMenuVisible;
        barContainer.clearActions();
        float targetY = isFilterMenuVisible ? 30f : -barContainer.getHeight() - 50f;
        barContainer.addAction(Actions.moveTo(barContainer.getX(), targetY, 0.4f, Interpolation.pow2Out));
    }

    private Label createCollectionLabel() {
        int collected = App.getActiveUser().getUnlockedPlants().size();
        int total = App.getAllPlants().size();

        Label collectionLabel = new Label("Plants Collected: " + collected + " of " + total, skin, "medium");
        collectionLabel.setColor(Color.valueOf("#4A3018"));
        collectionLabel.setFontScale(1.1f);

        return collectionLabel;
    }

    private void setupListeners(TextButton toggleBtn, TextButton filterMenuBtn, ImageButton closeBtn,
                                ScrollPane scrollPane, Table plantsTable, Table zombiesTable) {
        toggleBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleAlmanacView(toggleBtn, filterMenuBtn, scrollPane, plantsTable, zombiesTable);
            }
        });

        filterMenuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleFilterMenu();
            }
        });

        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                ScreenManager.getInstance().popScreen();
            }
        });
    }

    private void toggleAlmanacView(TextButton toggleBtn, TextButton filterMenuBtn, ScrollPane scrollPane,
                                   Table plantsTable, Table zombiesTable) {
        isShowingPlants = !isShowingPlants;

        if (isShowingPlants) {
            scrollPane.setActor(plantsTable);
            toggleBtn.setText("Zombies");
            filterMenuBtn.setVisible(true);
            if (isFilterMenuVisible) {
                barContainer.addAction(Actions.moveTo(barContainer.getX(), 30f, 0.4f, Interpolation.pow2Out));
            }
        } else {
            scrollPane.setActor(zombiesTable);
            toggleBtn.setText("Plants");
            filterMenuBtn.setVisible(false);
            barContainer.addAction(Actions.moveTo(barContainer.getX(), -barContainer.getHeight() - 50f,
                0.4f, Interpolation.pow2Out));
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



        for (PlantCardButton card : allPlantCards) {
            if (shouldShowCard(card)) {
                table.add(card).size(150, 115).expandX().padBottom(20);
                count++;

                if (count % columns == 0) table.row();
            }
        }
    }

    private boolean shouldShowCard(PlantCardButton card) {
        if (currentFilterState == FilterState.ALL) return true;
        if (currentFilterState == FilterState.UNLOCKED) return App.getActiveUser().isItUnlocked(card.getPlant());
        if (currentFilterState == FilterState.UPGRADABLE) return card.isReadyToUpgrade();
        if (currentFilterState == FilterState.CATEGORY) {
            return selectedCategories.contains(card.getPlant().getCategory());
        }
        return false;
    }

    private Table buildPlantsTable(TextureBank textures) {
        Table table = new Table();
        table.top();
        User currentUser = App.getActiveUser();

        if (allPlantCards.isEmpty()) {
            for (Plant plant : App.getAllPlants()) {
                if (currentUser.isItUnlocked(plant)) {
                    Plant foundPlant = currentUser.getUnlockedPlants().stream()
                        .filter(p -> p.getName().equals(plant.getName()))
                        .findFirst()
                        .orElse(null);
                    if (foundPlant != null) {
                        createPlantCard(textures, foundPlant);
                    }
                } else {
                    createPlantCard(textures, plant);
                }
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
            if(ZombieType.isItZombotany(zombie.getType()))
                continue;
            Image background = UiFactory.imageFor(textures, "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_READY");
            String zombiePath = "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_" + UiFactory.getZombieAddress(zombie);
            Image zombieImage = (App.getActiveUser().isZombieUnlocked(zombie)) ?
                UiFactory.imageFor(textures, zombiePath) : null;

            if (background != null) {
                ZombieCardButton card = new ZombieCardButton(background, zombieImage, zombie, skin);
                table.add(card).size(card.getWidth(), card.getHeight()).expandX()
                    .padBottom(25).padLeft(10).padRight(10);
                count++;

                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        ZombieInfoScreen infoScreen = new ZombieInfoScreen(game, skin, zombie);
                        ScreenManager.getInstance().pushScreen(infoScreen);
                    }
                });

                if (count % columns == 0) table.row();
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

            if (plantImg == null || familyImg == null) throw new NullPointerException("Image is null");

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
                    PlantInfoScreen infoScreen = new PlantInfoScreen(game, skin, plant, card, controller);
                    ScreenManager.getInstance().pushScreen(infoScreen);
                }
            });

            return card;
        } catch (Exception e) {
            return null;
        }
    }

    private String getCardAddress(Plant plant) {
        List<PlantTag> tags = plant.getTags();
        if (tags.contains(PlantTag.ICE)) return "IMAGE_UI_PACKETS_ICEAGE";
        if (tags.contains(PlantTag.WATER)) return "IMAGE_UI_PACKETS_BEACH";
        if (tags.contains(PlantTag.EXPLOSIVE) || plant.getCategory() == PlantCategory.EXPLOSIVE) {
            return "IMAGE_UI_PACKETS_DINO";
        }
        if (tags.contains(PlantTag.MAGIC)) return "IMAGE_UI_PACKETS_EIGHTIES";
        if (tags.contains(PlantTag.NIGHT)) return "IMAGE_UI_PACKETS_DARK";
        if (tags.contains(PlantTag.CHARGE)) return "IMAGE_UI_PACKETS_FUTURE";
        if (tags.contains(PlantTag.TRAP)) return "IMAGE_UI_PACKETS_EGYPT";
        if (plant.getCategory() == PlantCategory.WALL_NUT) return "IMAGE_UI_PACKETS_COWBOY";
        if (plant.getCategory() == PlantCategory.SHOOTER) return "IMAGE_UI_PACKETS_LOSTCITY";
        if (plant.getCategory() == PlantCategory.SUN_PRODUCER) return "IMAGE_UI_PACKETS_BOOST";

        return "IMAGE_UI_PACKETS_HOMELESS";
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
            case EXPLOSIVE -> "IMAGE_UI_PACKETS_MINTFAM_EXPLOSIVE";
        };
    }
}
