package io.java.pvz.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.timeManager.TimeManager;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PlantCardButton;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

import java.util.List;

import static com.badlogic.gdx.Gdx.input;
import static io.java.pvz.models.enums.PhysicalConstants.*;

public class GameFlowScreen extends BaseScreen {

    private TextureRegion mainRegion;
    private TextureRegion leftRegion;
    private TextureRegion rightRegion;

    private Plant selectedPlantToPlace = null;
    private Image floatingPlantImage = null;

    private boolean isShovelSelected = false;
    private Image floatingShovelImage = null;

    private ShapeRenderer shapeRenderer;
    private BitmapFont debugFont;

    private static final int COLS = 9;
    private static final int ROWS = 5;

    private String currentMapId;

    private static final float TICK_DURATION = 1f / TimeManager.TICKS_PER_SECOND;
    private float simulationAccumulator = 0f;
    private BattlefieldRenderer battlefieldRenderer;

    private final GameFlowController gameFlowController = new GameFlowController();

    public GameFlowScreen(Game game, String mapTextureId) {
        super(game);
        loadMap(mapTextureId);
        buildUI();

        shapeRenderer = new ShapeRenderer();
        debugFont = new BitmapFont();
        debugFont.getData().setScale(1.2f);

        camera.position.x = viewport.getWorldWidth();
        camera.update();
    }

    private void loadMap(String mainMapId) {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        this.currentMapId = mainMapId;
        mainRegion = textures.region(mainMapId);
        leftRegion = textures.region(mainMapId + "_LEFT");
        rightRegion = textures.region(mainMapId + "_RIGHT");

        if (mainRegion == null) System.err.println("⚠️ Warning: Map main texture not found: " + mainMapId);
        if (leftRegion == null) System.err.println("⚠️ Warning: Map left texture not found: " + mainMapId + "_LEFT");
        if (rightRegion == null) System.err.println("⚠️ Warning: Map right texture not found: " + mainMapId + "_RIGHT");
    }

    private void buildUI() {
        Skin skin = AssetLoader.getInstance().getSkin();
        TextureBank textures = AssetLoader.getInstance().getTextures();

        mainLayer.clear();
        mainLayer.setFillParent(true);

        battlefieldRenderer = new BattlefieldRenderer();
        mainLayer.addActor(battlefieldRenderer.getGroup());

        setupPlantSelectionMenu(skin, textures);
        setupIndicators(skin, textures);
        setupActionButtons(skin, textures);
    }

    private void setupPlantSelectionMenu(Skin skin, TextureBank textures) {
        if (App.getActiveMenu() == Menu.PLANTSELLECTION_MENU) {
            PlantSelectionModalTable plantSelectionModal = new PlantSelectionModalTable(skin, () -> {
                buildSeedBank(skin, textures);
            });
            plantSelectionModal.show(modalLayer, viewport);
        } else {
            buildSeedBank(skin, textures);
        }
    }

    private void setupIndicators(Skin skin, TextureBank textures) {
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

        Table plantFoodTable = new Table();
        plantFoodTable.setPosition(100, 850);
        plantFoodTable.left();

        Image plantFoodIcon = UiFactory.imageFor(textures, Ids.UI.PLANT_FOOD_ICON);

        Label plantFoodLabel = new Label("0", skin , "medium_outline") {
            @Override
            public void act(float delta) {
                super.act(delta);
                if (App.getActiveUser() != null) {
                    setText("Count: "+String.valueOf(App.getActiveUser().getPlantFoodCount()));
                }
            }
        };
        plantFoodLabel.setFontScale(1f);
        plantFoodTable.add(plantFoodIcon).size(50, 40);
        plantFoodTable.add(plantFoodLabel).padLeft(15);

        mainLayer.addActor(plantFoodTable);
    }

    private void setupActionButtons(Skin skin, TextureBank textures) {
        Stack nukeBtn = UiFactory.iconButton(textures, skin, Ids.UI.NUKE_BUTTON, 150, 150, () -> {}, false);
        nukeBtn.setPosition(150, 150);
        mainLayer.addActor(nukeBtn);

        Stack shovelBtn = UiFactory.iconButton(textures, skin, Ids.UI.SHOVEL, 110, 110, () -> {
            if (floatingPlantImage != null) {
                floatingPlantImage.remove();
                floatingPlantImage = null;
                selectedPlantToPlace = null;
            }

            if (isShovelSelected) {
                isShovelSelected = false;
                if (floatingShovelImage != null) {
                    floatingShovelImage.remove();
                    floatingShovelImage = null;
                }
            } else {
                isShovelSelected = true;
                floatingShovelImage = new Image(UiFactory.imageFor(textures, Ids.UI.FLOATING_SHOVEL).getDrawable()) {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                        viewport.unproject(mousePos);
                        setPosition(mousePos.x - getWidth() / 2f, mousePos.y - getHeight() / 2f);
                    }
                };
                floatingShovelImage.setSize(80, 80);
                floatingShovelImage.setTouchable(Touchable.disabled);
                mainLayer.addActor(floatingShovelImage);
            }
        }, false);
        shovelBtn.setPosition(1350, 855);
        mainLayer.addActor(shovelBtn);

        Stack pauseBtn = UiFactory.iconButton(textures, skin, Ids.UI.PAUSE, 90, 90, () -> {
            new PauseMenuTable(skin).show(modalLayer, viewport);
        });
        pauseBtn.setPosition(1730, 950);
        mainLayer.addActor(pauseBtn);

        Stack fastForwardBtn = UiFactory.iconButton(textures, skin, Ids.UI.FAST_FORWARD, 90, 90, () -> {});
        fastForwardBtn.setPosition(1620, 950);
        mainLayer.addActor(fastForwardBtn);
    }

    private void buildSeedBank(Skin skin, TextureBank textures) {
        Table seedBankTable = new Table();
        seedBankTable.setPosition(310f, 820f);
        seedBankTable.setSize(790f, 250f);
        seedBankTable.top().left();
        seedBankTable.pad(20f);

        List<Plant> selectedPlants = GameSession.getInstance().getChosenPlants();
        int count = 0;

        for (Plant plant : selectedPlants) {
            count++;
            PlantCardButton plantButton = createSeedPacket(plant, skin, textures);
            seedBankTable.add(plantButton).size(170f, 100f).padRight(15f).padBottom(20f);

            if (count % 4 == 0) {
                seedBankTable.row();
            }
        }
        mainLayer.addActor(seedBankTable);
    }

    private PlantCardButton createSeedPacket(Plant plant, Skin skin, TextureBank textures) {
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
            .build();

        plantButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isShovelSelected) {
                    isShovelSelected = false;
                    if (floatingShovelImage != null) {
                        floatingShovelImage.remove();
                        floatingShovelImage = null;
                    }
                }
                if (floatingPlantImage != null) {
                    floatingPlantImage.remove();
                }

                selectedPlantToPlace = plant;

                floatingPlantImage = new Image(plantIcon.getDrawable()) {
                    @Override
                    public void act(float delta) {
                        super.act(delta);
                        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                        viewport.unproject(mousePos);
                        setPosition(mousePos.x - getWidth() / 2f, mousePos.y - getHeight() / 2f);
                    }
                };

                floatingPlantImage.setSize(80, 80);
                floatingPlantImage.setTouchable(Touchable.disabled);
                mainLayer.addActor(floatingPlantImage);
            }
        });

        return plantButton;
    }

    @Override
    public void render(float delta) {
        clearScreen(0.1f, 0.1f, 0.1f, 1f);
        AssetLoader.getInstance().updateTextures();

        handlePlantPlacement();
        handleShovelAction();
        handleCameraMovement(delta);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        drawBackground();
        handleDebugSpawnKeys();
        batch.end();

        drawDebugLayout();
        advanceSimulation(delta);

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }

    private void handleCameraMovement(float delta) {
        camera.update();
        float screenH = viewport.getWorldHeight();
        float leftDrawW = leftRegion.getRegionWidth() * (screenH / leftRegion.getRegionHeight());
        float mainDrawW = mainRegion.getRegionWidth() * (screenH / mainRegion.getRegionHeight());
        float rightDrawW = rightRegion.getRegionWidth() * (screenH / rightRegion.getRegionHeight());

        if (currentMapId != null && currentMapId.toLowerCase().contains("ice")) {
            mainDrawW += 25;
        }

        if (input.isKeyPressed(Input.Keys.RIGHT) &&
            camera.position.x < leftDrawW + rightDrawW + mainDrawW - viewport.getWorldWidth() / 2f)
            camera.position.x += 300 * delta;
        if (input.isKeyPressed(Input.Keys.LEFT) && camera.position.x > viewport.getWorldWidth() / 2f)
            camera.position.x -= 300 * delta;
    }

    private void drawBackground() {
        float screenH = viewport.getWorldHeight();
        float currentX = 0;

        float leftScale = screenH / leftRegion.getRegionHeight();
        float leftDrawW = leftRegion.getRegionWidth() * leftScale;
        batch.draw(leftRegion, currentX, 0, leftDrawW, screenH);
        currentX += leftDrawW;

        float mainScale = screenH / mainRegion.getRegionHeight();
        float mainDrawW = mainRegion.getRegionWidth() * mainScale;
        float mainDrawY = 0f;
        float mainDrawH = screenH;

        if (currentMapId != null && currentMapId.toLowerCase().contains("ice")) {
            mainDrawY = -25;
            mainDrawH = screenH + 25;
            mainDrawW += 25;
        }

        batch.draw(mainRegion, currentX, mainDrawY, mainDrawW, mainDrawH);
        currentX += mainDrawW;

        float rightScale = screenH / rightRegion.getRegionHeight();
        float rightDrawW = rightRegion.getRegionWidth() * rightScale;
        batch.draw(rightRegion, currentX, 0, rightDrawW, screenH);
    }

    private void advanceSimulation(float delta) {
        GameSession session = GameSession.getInstance();
        if (session == null) return;

        simulationAccumulator += delta;
        while (simulationAccumulator >= TICK_DURATION) {
            session.update(1);
            simulationAccumulator -= TICK_DURATION;
        }

        battlefieldRenderer.sync(session.getArena());
    }

    private void handleDebugSpawnKeys() {
        if (GameSession.getInstance() == null) return;

        if (input.isKeyJustPressed(Input.Keys.P)) {
            gameFlowController.plantPlant("Peashooter", "1", "1");
        }

        if (input.isKeyJustPressed(Input.Keys.Z)) {
            gameFlowController.cheatSpawnZombie("Normal", "9", "1");
        }
    }

    private void drawDebugLayout() {
        drawDebugShapes();
        drawDebugText();
    }

    private void drawDebugShapes() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.GREEN);
        for (int i = 0; i <= COLS; i++) {
            float vx = GRID_START_X + (i * TILE_WIDTH);
            shapeRenderer.line(vx, GRID_START_Y, vx, GRID_START_Y + (ROWS * TILE_HEIGHT));
        }
        for (int i = 0; i <= ROWS; i++) {
            float hy = GRID_START_Y + (i * TILE_HEIGHT);
            shapeRenderer.line(GRID_START_X, hy, GRID_START_X + (COLS * TILE_WIDTH), hy);
        }
        shapeRenderer.setColor(Color.RED);
        float mowerWidth = 100f;
        float mowerX = GRID_START_X - mowerWidth - 30;
        for (int i = 0; i < ROWS; i++) {
            float mowerY = GRID_START_Y + (i * TILE_HEIGHT) + 20;
            shapeRenderer.rect(mowerX, mowerY, mowerWidth, TILE_HEIGHT - 40);
        }
        shapeRenderer.setColor(Color.PURPLE);
        float zombieSpawnX = GRID_START_X + (COLS * TILE_WIDTH) + 100;
        shapeRenderer.rect(zombieSpawnX, GRID_START_Y, 700, ROWS * TILE_HEIGHT);

        for (int i = 0; i < ROWS; i++) {
            float firstColY = GRID_START_Y + (i * TILE_HEIGHT) + TILE_HEIGHT / 2f;
            shapeRenderer.line(zombieSpawnX + 50, firstColY, zombieSpawnX + 600, firstColY);
        }
        shapeRenderer.setColor(Color.YELLOW);
        shapeRenderer.rect(30, 950, 180, 80);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(30, 850, 220, 80);

        shapeRenderer.setColor(Color.BROWN);
        float bankX = 310f, bankY = 820;
        shapeRenderer.rect(bankX, bankY, 790, 250);
        for (int r = 0; r < 2; r++) {
            for (int c = 0; c < 4; c++) {
                shapeRenderer.rect(bankX + 20 + (c * 185), bankY + 20 + (r * 120), 170, 100);
            }
        }

        shapeRenderer.setColor(Color.ORANGE);
        shapeRenderer.rect(1400f, 30f, 400, 45);
        shapeRenderer.rect(150, 150, 150, 150);
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(1350f, 855f, 110, 110);
        shapeRenderer.setColor(Color.MAGENTA);
        shapeRenderer.rect(1620f, 950f, 250, 90);
        shapeRenderer.end();
    }

    private void drawDebugText() {
        batch.begin();
        debugFont.setColor(Color.WHITE);

        for (int i = 0; i <= COLS; i++) {
            float vx = GRID_START_X + (i * TILE_WIDTH);
            debugFont.draw(batch, "X:" + (int) vx, vx - 15, GRID_START_Y + (ROWS * TILE_HEIGHT) + 30);
        }

        float zombieSpawnX = GRID_START_X + (COLS * TILE_WIDTH) + 100;
        for (int i = 0; i <= ROWS; i++) {
            float hy = GRID_START_Y + (i * TILE_HEIGHT);
            debugFont.draw(batch, "Y:" + (int) hy, GRID_START_X - 60, hy + 5);
            float firstColY = GRID_START_Y + (i * TILE_HEIGHT) + TILE_HEIGHT / 2f;
            if (i < ROWS) {
                debugFont.draw(batch, "Zombie Rows Y\n(Y:" + (int) firstColY + ")" +
                    "\n from x = " + (zombieSpawnX + 20)
                    + " to x = " + (zombieSpawnX + 620), zombieSpawnX + 20, firstColY);
            }
        }

        float mowerX = GRID_START_X - 100f - 30;
        debugFont.draw(batch, "Mowers\nX:" + (int) mowerX, mowerX, GRID_START_Y - 20);
        debugFont.draw(batch, "Sun\n(30, 950)", 40, 1010);
        debugFont.draw(batch, "PlantFood\n(30, 850)", 40, 910);
        debugFont.draw(batch, "Seed Bank (X:310 , Y:820)", 310f, 810f);
        debugFont.draw(batch, "Wave Progress\n(X:1400 , Y:30)", 1400f, 110f);
        debugFont.draw(batch, "release the nuke\n(X:150, Y:150)", 150f, 135f);
        debugFont.draw(batch, "Shovel\n(X:1350, Y:855)", 1350f, 840f);
        debugFont.draw(batch, "Controls (Pause/FF)\n(X:1620, Y:950)", 1620f, 935f);

        batch.end();
    }

    private void handlePlantPlacement() {
        if (selectedPlantToPlace == null || floatingPlantImage == null) return;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            floatingPlantImage.remove();
            floatingPlantImage = null;
            selectedPlantToPlace = null;
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mousePos);

            float x = mousePos.x;
            float y = mousePos.y;

            if (x >= GRID_START_X && x <= GRID_START_X + (COLS * TILE_WIDTH) &&
                y >= GRID_START_Y && y <= GRID_START_Y + (ROWS * TILE_HEIGHT)) {

                int col = (int) ((x - GRID_START_X) / TILE_WIDTH) + 1;
                int row = (int) ((y - GRID_START_Y) / TILE_HEIGHT) + 1;

                Result result = gameFlowController.plantPlant(selectedPlantToPlace.getName(),
                    String.valueOf(col), String.valueOf(row));

                if (result.isSuccessful()) {
                    floatingPlantImage.remove();
                    floatingPlantImage = null;
                    selectedPlantToPlace = null;
                }
            }
        }
    }

    private void handleShovelAction() {
        if (!isShovelSelected || floatingShovelImage == null) return;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            floatingShovelImage.remove();
            floatingShovelImage = null;
            isShovelSelected = false;
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            viewport.unproject(mousePos);

            float x = mousePos.x;
            float y = mousePos.y;

            if (x >= GRID_START_X && x <= GRID_START_X + (COLS * TILE_WIDTH) &&
                y >= GRID_START_Y && y <= GRID_START_Y + (ROWS * TILE_HEIGHT)) {

                int col = (int) ((x - GRID_START_X) / TILE_WIDTH) + 1;
                int row = (int) ((y - GRID_START_Y) / TILE_HEIGHT) + 1;

                Result result = gameFlowController.pluckPlant(String.valueOf(col), String.valueOf(row));

                if (result.isSuccessful()) {
                    floatingShovelImage.remove();
                    floatingShovelImage = null;
                    isShovelSelected = false;
                }
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (debugFont != null) debugFont.dispose();
    }
}
