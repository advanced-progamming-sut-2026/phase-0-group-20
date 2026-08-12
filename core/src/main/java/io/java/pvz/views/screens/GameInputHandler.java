package io.java.pvz.views.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.controllers.GameController.MiniGameController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.speciallevels.ConveyorBelt;
import io.java.pvz.models.game.minigame.BowlingLevel;
import io.java.pvz.models.game.minigame.DroppedSeedPacket;
import io.java.pvz.models.game.minigame.VaseBreakerLevel;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

import static io.java.pvz.models.enums.PhysicalConstants.*;

public class GameInputHandler {
    private final Group mainLayer;
    private final Viewport viewport;
    private final GameFlowController gameFlowController;
    private final MiniGameController miniGameController;
    private GameHUD gameHUD;

    private Zombie selectedZombieToPlace = null;
    private Plant selectedPlantToPlace = null;
    private DroppedSeedPacket selectedPacketToPlace = null;
    private Image floatingPlantImage = null;

    private boolean isShovelSelected = false;
    private Image floatingShovelImage = null;

    private boolean isPlantFoodSelected = false;
    private Image floatingPlantFoodImage = null;

    private final Image rowHighlight;
    private final Image colHighlight;

    private static final int COLS = 9;
    private static final int ROWS = 5;

    public GameInputHandler(Group mainLayer, Viewport viewport, Group highlightLayer, GameFlowController gameFlowController, MiniGameController miniGameController) {
        this.mainLayer = mainLayer;
        this.viewport = viewport;
        this.gameFlowController = gameFlowController;
        this.miniGameController = miniGameController;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.35f);
        pixmap.fill();
        Texture highlightTex = new Texture(pixmap);
        pixmap.dispose();

        rowHighlight = new Image(highlightTex);
        colHighlight = new Image(highlightTex);
        rowHighlight.setVisible(false);
        colHighlight.setVisible(false);

        highlightLayer.addActor(rowHighlight);
        highlightLayer.addActor(colHighlight);
    }

    public void setGameHUD(GameHUD gameHUD) {
        this.gameHUD = gameHUD;
    }

    public void clearAllSelections() {
        if (floatingPlantImage != null) floatingPlantImage.remove();
        if (floatingShovelImage != null) floatingShovelImage.remove();
        if (floatingPlantFoodImage != null) floatingPlantFoodImage.remove();

        floatingPlantImage = null;
        floatingShovelImage = null;
        floatingPlantFoodImage = null;
        selectedZombieToPlace = null;
        selectedPlantToPlace = null;
        selectedPacketToPlace = null;
        isShovelSelected = false;
        isPlantFoodSelected = false;
    }

    private Image createFloatingImage(Drawable drawable, float size) {
        Image image = new Image(drawable) {
            @Override
            public void act(float delta) {
                super.act(delta);
                Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
                viewport.unproject(mousePos);
                setPosition(mousePos.x - getWidth() / 2f, mousePos.y - getHeight() / 2f);
            }
        };
        image.setSize(size, size);
        image.setTouchable(Touchable.disabled);
        mainLayer.addActor(image);
        return image;
    }

    public void onZombieCardClicked(Zombie zombie, Image zombieIcon) {
        clearAllSelections();
        selectedZombieToPlace = zombie;
        floatingPlantImage = createFloatingImage(zombieIcon.getDrawable(), 80);
    }

    public void onPlantCardClicked(Plant plant, Image plantIcon) {
        clearAllSelections();
        selectedPlantToPlace = plant;
        floatingPlantImage = createFloatingImage(plantIcon.getDrawable(), 80);
    }

    public void onDroppedPacketClicked(Plant plant, DroppedSeedPacket packet, Image plantIcon) {
        clearAllSelections();
        selectedPlantToPlace = plant;
        selectedPacketToPlace = packet;
        floatingPlantImage = createFloatingImage(plantIcon.getDrawable(), 80);
    }

    public void onShovelClicked() {
        boolean wasSelected = isShovelSelected;
        clearAllSelections();
        if (!wasSelected) {
            isShovelSelected = true;
            TextureBank textures = AssetLoader.getInstance().getTextures();
            floatingShovelImage = createFloatingImage(UiFactory.imageFor(textures, Ids.UI.FLOATING_SHOVEL).getDrawable(), 80);
        }
    }

    public void onPlantFoodClicked() {
        boolean wasSelected = isPlantFoodSelected;
        clearAllSelections();
        if (!wasSelected && App.getActiveUser().getPlantFoodCount() > 0) {
            isPlantFoodSelected = true;
            TextureBank textures = AssetLoader.getInstance().getTextures();
            floatingPlantFoodImage = createFloatingImage(UiFactory.imageFor(textures, "IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK_COLLECT").getDrawable(), 20);
        }
    }

    private Vector2 getGridPosition() {
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        if (mousePos.x >= GRID_START_X && mousePos.x <= GRID_START_X + (COLS * TILE_WIDTH) &&
            mousePos.y >= GRID_START_Y && mousePos.y <= GRID_START_Y + (ROWS * TILE_HEIGHT)) {
            float col = (mousePos.x - GRID_START_X) / TILE_WIDTH;
            float row = (mousePos.y - GRID_START_Y) / TILE_HEIGHT;
            return new Vector2(col, row);
        }
        return null;
    }

    public void handleTileClick() {
        if (GameSession.getInstance() == null) return;

        if (Gdx.input.isButtonJustPressed(Input.Buttons.RIGHT)) {
            clearAllSelections();
            return;
        }

        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            Vector2 gridPos = getGridPosition();
            if (gridPos == null) return;

            int col = (int) gridPos.x + 1;
            int row = (int) gridPos.y + 1;

            if (isShovelSelected) {
                if (gameFlowController.pluckPlant(String.valueOf(col), String.valueOf(row)).isSuccessful()) {
                    clearAllSelections();
                }
                return;
            }

            if (isPlantFoodSelected) {
                if (gameFlowController.feedPlant(String.valueOf(col), String.valueOf(row)).isSuccessful()) {
                    clearAllSelections();
                    if (gameHUD != null) gameHUD.updatePlantFoodCount();
                }
                return;
            }

            if (selectedZombieToPlace != null && floatingPlantImage != null) {
                handleZombiePlacement(col, row);
                return;
            }

            if (selectedPlantToPlace != null && floatingPlantImage != null) {
                handlePlanting(col, row);
                return;
            }

            if (GameSession.getInstance().getCurrentMode() instanceof VaseBreakerLevel) {
                miniGameController.breakVase(String.valueOf(col), String.valueOf(row));
            }
        }
    }

    private void handleZombiePlacement(int col, int row) {
        String alias = selectedZombieToPlace.getType().getJsonAlias();
        Result result = miniGameController.handlePutZombie(alias, String.valueOf(col), String.valueOf(row));

        System.out.println(result.message());

        if (result.isSuccessful())
            clearAllSelections();
    }

    private void handlePlanting(int col, int row) {
        Result result;
        if (selectedPacketToPlace != null) {
            result = miniGameController.plantFromVase(
                String.valueOf(selectedPacketToPlace.getCol() + 1),
                String.valueOf(selectedPacketToPlace.getRow() + 1),
                String.valueOf(col), String.valueOf(row)
            );
        } else {
            if(GameSession.getInstance().getCurrentMode() instanceof BowlingLevel) {
                result = miniGameController.plantBowlingNut(selectedPlantToPlace, col, row);
                System.out.println(result.message());

            }else{
                result = gameFlowController.plantPlant(selectedPlantToPlace.getName(),
                    String.valueOf(col), String.valueOf(row));
            }
        }

        if (result.isSuccessful()) {
            if (GameSession.getInstance().getCurrentMode() instanceof ConveyorBelt beltLevel) {
                beltLevel.getBelt().remove(selectedPlantToPlace);
            }
            clearAllSelections();
        }
    }

    public void updatePlantingHighlights() {
        if (isShovelSelected || isPlantFoodSelected || selectedPlantToPlace != null) {
            Vector2 gridPos = getGridPosition();
            if (gridPos != null) {
                int col = (int) gridPos.x;
                int row = (int) gridPos.y;

                rowHighlight.setSize(COLS * TILE_WIDTH, TILE_HEIGHT);
                rowHighlight.setPosition(GRID_START_X, GRID_START_Y + (row * TILE_HEIGHT));
                rowHighlight.setVisible(true);

                colHighlight.setSize(TILE_WIDTH, ROWS * TILE_HEIGHT);
                colHighlight.setPosition(GRID_START_X + (col * TILE_WIDTH), GRID_START_Y);
                colHighlight.setVisible(true);
                return;
            }
        }
        rowHighlight.setVisible(false);
        colHighlight.setVisible(false);
    }
}
