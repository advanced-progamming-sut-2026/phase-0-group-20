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


        if (App.getActiveMenu() == Menu.PLANTSELLECTION_MENU) {

            PlantSelectionModalTable plantSelectionModal = new PlantSelectionModalTable(skin, () -> {
                buildSeedBank(skin, textures);
            });
            plantSelectionModal.show(modalLayer, viewport);
        } else {

            buildSeedBank(skin, textures);
        }

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
                Result result = gameFlowController.cheatAddSun("50");
                System.out.println(result.message());
            }
        });

        mainLayer.addActor(sunStack);
        Image plantFoodIcon = UiFactory.imageFor(textures, Ids.UI.PLANT_FOOD_ICON);
        plantFoodIcon.setSize(100, 80);
        plantFoodIcon.setPosition(100, 850);
        mainLayer.addActor(plantFoodIcon);

        Stack nukeBtn = UiFactory.iconButton(textures, skin, Ids.UI.NUKE_BUTTON, 150, 150, () -> {
            System.out.println("Nuke launched!");
        }, false);
        nukeBtn.setPosition(150, 150);
        mainLayer.addActor(nukeBtn);

        Stack shovelBtn = UiFactory.iconButton(textures, skin, Ids.UI.SHOVEL, 110, 110, () -> {
            System.out.println("Shovel picked up!");
        }, false);
        shovelBtn.setPosition(1350, 855);
        mainLayer.addActor(shovelBtn);

        Stack pauseBtn = UiFactory.iconButton(textures, skin, Ids.UI.PAUSE, 90, 90, () -> {
            System.out.println("Game Paused");
            new PauseMenuTable(skin).show(modalLayer, viewport);
        });
        pauseBtn.setPosition(1730, 950);
        mainLayer.addActor(pauseBtn);

        Stack fastForwardBtn = UiFactory.iconButton(textures, skin, Ids.UI.FAST_FORWARD, 90, 90, () -> {
            System.out.println("Speed toggled!");
        });
        fastForwardBtn.setPosition(1620, 950);
        mainLayer.addActor(fastForwardBtn);

    }


    private void buildSeedBank(Skin skin, TextureBank textures) {
        Table seedBankTable = new Table();
        seedBankTable.setPosition(310f, 820f);
        seedBankTable.setSize(790f, 250f);
        seedBankTable.top().left();
        seedBankTable.pad(20f);

        int columns = 4;


        List<Plant> selectedPlants = GameSession.getInstance().getChosenPlants();
        int count = 0;

        for (Plant plant : selectedPlants) {
            count++;
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

                    System.out.println("Selected for placement: " + plant.getName());
                }
            });

            seedBankTable.add(plantButton).size(170f, 100f).padRight(15f).padBottom(20f);

            if (count % columns == 0) {
                seedBankTable.row();
            }
        }

        mainLayer.addActor(seedBankTable);
    }

    @Override
    public void render(float delta) {
        clearScreen(0.1f, 0.1f, 0.1f, 1f);
        AssetLoader.getInstance().updateTextures();
        handlePlantPlacement();
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

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

        if (input.isKeyPressed(Input.Keys.RIGHT) &&
            camera.position.x < leftDrawW + rightDrawW + mainDrawW - viewport.getWorldWidth() / 2f)
            camera.position.x += 300 * delta;
        if (input.isKeyPressed(Input.Keys.LEFT) && camera.position.x > viewport.getWorldWidth() / 2f)
            camera.position.x -= 300 * delta;

        handleDebugSpawnKeys();
        batch.end();
        drawDebugLayout();
        advanceSimulation(delta);

        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
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
            Result result = gameFlowController.plantPlant("Peashooter", "1", "1");
            System.out.println(result.message());
        }

        if (input.isKeyJustPressed(Input.Keys.Z)) {
            Result result = gameFlowController.cheatSpawnZombie("Normal", "9", "1");
            System.out.println(result.message());
        }
    }

    private void drawDebugLayout() {
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
        for (int r = 0; r < 2; r++)
            for (int c = 0; c < 4; c++)
                shapeRenderer.rect(bankX + 20 + (c * 185), bankY + 20 + (r * 120), 170, 100);

        shapeRenderer.setColor(Color.ORANGE);
        float progX = 1400f, progY = 30f;
        shapeRenderer.rect(progX, progY, 400, 45);

        float nukX = 150;
        shapeRenderer.rect(nukX, nukX, nukX, nukX);

        shapeRenderer.setColor(Color.BLUE);
        float shovelX = 1350f, shovelY = 855f;
        shapeRenderer.rect(shovelX, shovelY, 110, 110);

        shapeRenderer.setColor(Color.MAGENTA);
        float controlsX = 1620f, controlsY = 950f;
        shapeRenderer.rect(controlsX, controlsY, 250, 90);

        shapeRenderer.end();

        batch.begin();
        debugFont.setColor(Color.WHITE);

        for (int i = 0; i <= COLS; i++) {
            float vx = GRID_START_X + (i * TILE_WIDTH);
            debugFont.draw(batch, "X:" + (int) vx, vx - 15, GRID_START_Y + (ROWS * TILE_HEIGHT) + 30);
        }

        for (int i = 0; i <= ROWS; i++) {
            float hy = GRID_START_Y + (i * TILE_HEIGHT);
            debugFont.draw(batch, "Y:" + (int) hy, GRID_START_X - 60, hy + 5);
            float firstColY = GRID_START_Y + (i * TILE_HEIGHT) + TILE_HEIGHT / 2f;
            if (i < ROWS)
                debugFont.draw(batch, "Zombie Rows Y\n(Y:" + (int) firstColY + ")" + "\n from x = " + (zombieSpawnX + 20)
                    + " to x = " + (zombieSpawnX + 620), zombieSpawnX + 20, firstColY);
        }

        debugFont.draw(batch, "Mowers\nX:" + (int) mowerX, mowerX, GRID_START_Y - 20);

        debugFont.draw(batch, "Sun\n(30, 950)", 40, 1010);
        debugFont.draw(batch, "PlantFood\n(30, 850)", 40, 910);
        debugFont.draw(batch, "Seed Bank (X:" + (int) bankX + " , Y:" + (int) bankY + ")", bankX, bankY - 10);
        debugFont.draw(batch, "Wave Progress\n(X:" + (int) progX + " , Y:" + (int) progY + ")", progX, progY + 80);
        debugFont.draw(batch, "release the nuke\n(X:" + (int) nukX + ", Y:" + (int) nukX + ")", nukX, nukX - 15);

        debugFont.draw(batch, "Shovel\n(X:" + (int) shovelX + ", Y:" + (int) shovelY + ")", shovelX, shovelY - 15);
        debugFont.draw(batch, "Controls (Pause/FF)\n(X:" + (int) controlsX + ", Y:" + (int) controlsY + ")",
            controlsX, controlsY - 15);

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

                Result result = gameFlowController.plantPlant(selectedPlantToPlace.getName(), String.valueOf(col), String.valueOf(row));
                System.out.println(result.message());

                if (result.isSuccessful()) {
                    floatingPlantImage.remove();
                    floatingPlantImage = null;
                    selectedPlantToPlace = null;
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
