package io.java.pvz.views.screens.gameflow;

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
import io.java.pvz.controllers.GameController.MatchController;
import io.java.pvz.controllers.GameController.MiniGameController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.InGameEntityGenerator;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.entities.zombies.Zombie;
import io.java.pvz.models.entities.zombies.ZombieType;
import io.java.pvz.models.fields.tiles.Tile;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.levels.speciallevels.ConveyorBelt;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.game.minigame.*;
import io.java.pvz.net.server.PlayerRole;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PamAnimatedActor;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.List;

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
    private final HashMap<Tile, PamAnimatedActor> plantFoodAnimations = new HashMap<>();

    private Vector2 selectedGridPos = null;

    private final Image rowHighlight;
    private final Image colHighlight;

    private static final int COLS = 9;
    private static final int ROWS = 5;

    private int couchZombieRow = 0;
    private int couchZombieCol = 7;
    private int couchSelectedZombieIndex = -1;

    private Image couchFloatingZombieImage = null;
    private int lastCouchFloatingZombieIndex = -1;

    public GameInputHandler(Group mainLayer, Viewport viewport, Group highlightLayer,
                            GameFlowController gameFlowController, MiniGameController miniGameController) {
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

        selectedGridPos = null;
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

    private Image createCouchGridFollowerImage(Drawable drawable, float size) {
        Image image = new Image(drawable) {
            @Override
            public void act(float delta) {
                super.act(delta);
                float tileX = GRID_START_X + (couchZombieCol * TILE_WIDTH) + (TILE_WIDTH / 2f);
                float tileY = GRID_START_Y + (couchZombieRow * TILE_HEIGHT) + (TILE_HEIGHT / 2f);
                setPosition(tileX - getWidth() / 2f, tileY - getHeight() / 2f);
            }
        };
        image.setSize(size, size * 1.6f);
        image.setTouchable(Touchable.disabled);
        image.getColor().a = 0.8f;
        mainLayer.addActor(image);
        return image;
    }

    public void onZombieCardClicked(Zombie zombie, Image zombieIcon) {
        if (MatchController.getInstance().isOnlineMatch() &&
            MatchController.getInstance().getCurrentRole() == PlayerRole.PLANT)
            return;

        clearAllSelections();
        selectedZombieToPlace = zombie;
        floatingPlantImage = createFloatingImage(zombieIcon.getDrawable(), 80);
    }

    public void onPlantCardClicked(Plant plant, Image plantIcon) {
        if (MatchController.getInstance().isOnlineMatch() &&
            MatchController.getInstance().getCurrentRole() == PlayerRole.ZOMBIE)
            return;

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

    public void onBeghouledUpgradeClicked(String plantName) {
        clearAllSelections();
        Result result = miniGameController.upgradeBeghouledPlants(plantName);

        GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
            new GameEventPayload.Builder(GameEvent.NOTIFY)
                .message(result.message())
                .build());

        if (result.isSuccessful() && gameHUD != null) {
            gameHUD.refreshSeedBank();
        }
    }

    public void onShovelClicked() {
        if (MatchController.getInstance().isOnlineMatch() &&
            MatchController.getInstance().getCurrentRole() == PlayerRole.ZOMBIE)
            return;

        boolean wasSelected = isShovelSelected;
        clearAllSelections();
        if (!wasSelected) {
            isShovelSelected = true;
            TextureBank textures = AssetLoader.getInstance().getTextures();
            floatingShovelImage =
                createFloatingImage(UiFactory.imageFor(textures, Ids.UI.FLOATING_SHOVEL).getDrawable(), 80);
        }
    }

    public void onPlantFoodClicked() {
        if (MatchController.getInstance().isOnlineMatch() &&
            MatchController.getInstance().getCurrentRole() == PlayerRole.ZOMBIE)
            return;

        boolean wasSelected = isPlantFoodSelected;
        clearAllSelections();
        if (!wasSelected && App.getActiveUser().getPlantFoodCount() > 0) {
            isPlantFoodSelected = true;
            TextureBank textures = AssetLoader.getInstance().getTextures();
            floatingPlantFoodImage = createFloatingImage(
                UiFactory.imageFor(textures, "IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON_DOWN").getDrawable(), 20
            );
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
                if (MatchController.getInstance().isOnlineMatch()) {
                    MatchController.getInstance().pluckPlant(col, row, response -> {
                        if (response.isSuccess())
                            clearAllSelections();
                    });
                } else {
                    if (gameFlowController.pluckPlant(String.valueOf(col), String.valueOf(row)).isSuccessful())
                        clearAllSelections();
                }
                return;
            }

            if (isPlantFoodSelected) {
                if (gameFlowController.feedPlant(String.valueOf(col), String.valueOf(row)).isSuccessful()) {
                    float tileX = GRID_START_X + (col - 1) * TILE_WIDTH;
                    float tileY = GRID_START_Y + (row - 1) * TILE_HEIGHT;

                    float offsetX = 10f;
                    float offsetY = 100f;
                    float centerX = tileX + (TILE_WIDTH / 2f) + offsetX;
                    float centerY = tileY + (TILE_HEIGHT / 2f) + offsetY;
                    PamAnimatedActor actor = PamAnimatedActor.createEffectAnimated
                        ("768/INITIAL/EFFECTS/PLANTFOOD_FX/PLANTFOOD_FX.PAM", "plantfood");
                    actor.setPosition(centerX, centerY);
                    mainLayer.addActor(actor);
                    Tile tile = GameSession.getInstance().getArena().getTile(row - 1, col - 1);
                    plantFoodAnimations.put(tile, actor);
                    clearAllSelections();
                    if (gameHUD != null) gameHUD.updatePlantFoodCount();

                }
                return;
            }

            if (selectedGridPos != null && floatingPlantImage != null) {
                Result result = miniGameController.swapPlants(
                    (int) selectedGridPos.x, (int) selectedGridPos.y
                    , col, row);
                if (!result.isSuccessful()) {
                    GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                        new GameEventPayload.Builder(GameEvent.NOTIFY)
                            .message(result.message())
                            .build());
                }
                clearAllSelections();
                return;
            }

            if (GameSession.getInstance().getCurrentMode() instanceof BeghouledLevel) {
                handleGridPickup(col, row);
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

    public void sickAnimations() {
        if (plantFoodAnimations.isEmpty()) return;
        GameSession session = GameSession.getInstance();
        if (session == null) return;
        for (Tile tile : plantFoodAnimations.keySet()) {
            if (tile.getPlants().isEmpty()) return;
            for (Plant plant : tile.getPlants()) {
                if (!plant.isBoosted()) {
                    PamAnimatedActor actor = plantFoodAnimations.get(tile);
                    mainLayer.removeActor(actor);
                    break;
                }
            }
        }
    }

    private void handleGridPickup(int col, int row) {
        Plant plantOnTile = miniGameController.getPlantAtTile(col, row);
        if (plantOnTile != null) {
            selectedGridPos = new Vector2(col, row);

            TextureBank textures = AssetLoader.getInstance().getTextures();
            String plantTextureKey = "IMAGE_UI_PACKETS_" + UiFactory.getAtlasName(plantOnTile).toUpperCase();
            Image plantIcon = UiFactory.imageFor(textures, plantTextureKey);

            if (plantIcon != null) {
                floatingPlantImage = createFloatingImage(plantIcon.getDrawable(), 80);
            }
        }
    }

    private void handleZombiePlacement(int col, int row) {
        String alias = selectedZombieToPlace.getType().getJsonAlias();

        if (MatchController.getInstance().isOnlineMatch()) {
            MatchController.getInstance().releaseZombie(alias, col, row, response -> {
                if (response.isSuccess()) clearAllSelections();
                else GameSession.notify("Error: " + response.getErrorMessage());
            });
        } else {
            Result result = miniGameController.handlePutZombie(alias, String.valueOf(col), String.valueOf(row));
            if (result.isSuccessful()) clearAllSelections();
            else System.out.println(result.message());
        }
    }

    private void handlePlanting(int col, int row) {

        if (GameSession.getInstance().getCurrentMode() instanceof IZombieLevel iZombieLevel) {
            int zeroBasedCol = col - 1;
            if (!iZombieLevel.isValidPlantPlacement(zeroBasedCol)) {
                GameSession.notify("You must plant behind the red line!");
                return;
            }
        }

        if (MatchController.getInstance().isOnlineMatch() && selectedPacketToPlace == null) {
            MatchController.getInstance().placePlant(selectedPlantToPlace.getName(), col, row, response -> {
                if (response.isSuccess()) {
                    if (GameSession.getInstance().getCurrentMode() instanceof IZombieLevel iZombieLevel) {
                        iZombieLevel.getBelt().remove(selectedPlantToPlace);
                    }
                    clearAllSelections();
                } else {
                    GameSession.notify("Error: " + response.getErrorMessage());
                }
            });
            return;
        }

        Result result;
        if (selectedPacketToPlace != null) {
            result = miniGameController.plantFromVase(
                String.valueOf(selectedPacketToPlace.getCol() + 1),
                String.valueOf(selectedPacketToPlace.getRow() + 1),
                String.valueOf(col), String.valueOf(row)
            );
        } else {
            if (GameSession.getInstance().getCurrentMode() instanceof BowlingLevel) {
                result = miniGameController.plantBowlingNut(selectedPlantToPlace, col, row);
            } else {
                result = gameFlowController.plantPlant(selectedPlantToPlace.getName(),
                    String.valueOf(col), String.valueOf(row));
            }
        }

        if (result.isSuccessful()) {
            if (GameSession.getInstance().getCurrentMode() instanceof ConveyorBelt beltLevel) {
                beltLevel.getBelt().remove(selectedPlantToPlace);
            } else if (GameSession.getInstance().getCurrentMode() instanceof IZombieLevel iZombieLevel) {
                iZombieLevel.getBelt().remove(selectedPlantToPlace);
            }
            clearAllSelections();
        }
    }

    public void updatePlantingHighlights() {
        if (isShovelSelected || isPlantFoodSelected || selectedPlantToPlace != null || selectedGridPos != null) {
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

    public void handleCouchPlayKeyboard() {
        if (!MatchController.getInstance().isCouchPlay()) return;
        if (GameSession.getInstance() != null &&
            !(GameSession.getInstance().getCurrentMode() instanceof IZombieLevel)) return;

        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) couchZombieRow = Math.max(0, couchZombieRow - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.W)) couchZombieRow = Math.min(ROWS - 1, couchZombieRow + 1);

        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) couchZombieCol = Math.max(3, couchZombieCol - 1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) couchZombieCol = Math.min(COLS - 1, couchZombieCol + 1);

        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                couchSelectedZombieIndex = i;
                if (gameHUD != null)
                    gameHUD.highlightSelectedZombieCard(couchSelectedZombieIndex);
                updateCouchFloatingZombie();
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (couchSelectedZombieIndex < 0)
                return;

            IZombieLevel level = (IZombieLevel) GameSession.getInstance().getCurrentMode();
            List<ZombieType> availableZombies = level.getZombiesForThisLevel();

            if (couchSelectedZombieIndex < availableZombies.size()) {
                ZombieType type = availableZombies.get(couchSelectedZombieIndex);
                Result res = miniGameController.handlePutZombie(type.getJsonAlias(),
                    String.valueOf(couchZombieCol + 1), String.valueOf(couchZombieRow + 1));

                if (res.isSuccessful()) {
                    couchSelectedZombieIndex = -1;
                    if (gameHUD != null) {
                        gameHUD.highlightSelectedZombieCard(couchSelectedZombieIndex);
                    }
                    updateCouchFloatingZombie();
                } else {
                    GameSession.notify(res.message());
                }
            }
        }


        rowHighlight.setSize(COLS * TILE_WIDTH, TILE_HEIGHT);
        rowHighlight.setPosition(GRID_START_X, GRID_START_Y + (couchZombieRow * TILE_HEIGHT));
        rowHighlight.setVisible(true);

        colHighlight.setSize(TILE_WIDTH, ROWS * TILE_HEIGHT);
        colHighlight.setPosition(GRID_START_X + (couchZombieCol * TILE_WIDTH), GRID_START_Y);
        colHighlight.setVisible(true);

    }

    private void updateCouchFloatingZombie() {
        if (couchFloatingZombieImage != null) {
            couchFloatingZombieImage.remove();
            couchFloatingZombieImage = null;
        }

        if (couchSelectedZombieIndex < 0 || GameSession.getInstance() == null) return;
        if (!(GameSession.getInstance().getCurrentMode() instanceof IZombieLevel level)) return;

        List<ZombieType> availableZombies = level.getZombiesForThisLevel();
        if (couchSelectedZombieIndex >= availableZombies.size()) return;

        ZombieType type = availableZombies.get(couchSelectedZombieIndex);
        Zombie sampleZombie = InGameEntityGenerator.getZombieForGame(type, 0);

        TextureBank textures = AssetLoader.getInstance().getTextures();
        String zombiePath = "IMAGE_UI_ALMANAC_PACKETS_ZOMBIES_" + UiFactory.getZombieAddress(sampleZombie);
        Image zombieIcon = UiFactory.imageFor(textures, zombiePath);
        couchFloatingZombieImage = createCouchGridFollowerImage(zombieIcon.getDrawable(), 80);

        lastCouchFloatingZombieIndex = couchSelectedZombieIndex;
    }
}
