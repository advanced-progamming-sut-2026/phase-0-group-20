package io.java.pvz.utils;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.GameController.NetworkController;
import io.java.pvz.controllers.GameController.TravelLogController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.adventure.Chapter;
import io.java.pvz.models.game.adventure.SeasonType;
import io.java.pvz.models.game.adventure.levels.Level;
import io.java.pvz.models.game.minigame.MiniGameFactory;
import io.java.pvz.models.game.minigame.MiniGameType;
import io.java.pvz.views.screens.ChapterSelectionScreen;
import io.java.pvz.views.screens.LevelSelectionScreen;
import io.java.pvz.views.screens.MultiplayerLobbyScreen;
import pvz.libpvz.textures.TextureBank;

public class MinigameItemUi extends Table {

    private Texture frameTexture;

    public MinigameItemUi(MiniGameType type, Skin skin, TextureBank textures, TravelLogController travelLogController) {

        Stack stack = new Stack();

        String bgAddress = getBackgroundAddress(type);
        Image cardBg = UiFactory.imageFor(textures, bgAddress);
        cardBg.setScaling(Scaling.stretch);
        stack.add(cardBg);

        Color boardColor = Color.valueOf("5c3a21");
        frameTexture = createRoundedFrameTexture(200, 100, 20, boardColor);
        Image roundedFrame = new Image(frameTexture);
        roundedFrame.setScaling(Scaling.stretch);
        stack.add(roundedFrame);

        Table contentTable = new Table();
        contentTable.pad(15, 40, 15, 40);

        Label nameLabel = new Label(type.getName(), skin, "FBUSV8C5EI_2", Color.WHITE);
        nameLabel.setFontScale(1.8f);

        TextButton playBtn = new TextButton("PLAY", skin, "purple");
        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (type == MiniGameType.I_ZOMBIE) {
                    ScreenManager.getInstance().pushScreen(
                        new MultiplayerLobbyScreen(ScreenManager.getInstance().getGame())
                    );
                } else {
                    ScreenManager.getInstance().pushScreen(
                        new LevelSelectionScreen(ScreenManager.getInstance().getGame(), type, travelLogController)
                    );
                }
            }
        });

        contentTable.add(nameLabel).left().expandX();
        contentTable.add(playBtn).size(140, 60).right();

        stack.add(contentTable);
        this.add(stack).expand().fill();
    }

    private Texture createRoundedFrameTexture(int width, int height, int radius, Color frameColor) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(frameColor);
        pixmap.fill();

        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(new Color(0, 0, 0, 0));

        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);

        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius, radius, radius);
        pixmap.fillCircle(radius, height - radius, radius);
        pixmap.fillCircle(width - radius, height - radius, radius);

        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private String getBackgroundAddress(MiniGameType type) {
        return switch (type) {
            case VASE_BREAKER -> "IMAGE_UI_FEATURE_UNLOCK_FEATURE_KEY_ART_VASEBREAKER";
            case BOWLING -> "IMAGE_UI_FEATURE_UNLOCK_FEATURE_KEY_ART_ZEN_GARDEN";
            case I_ZOMBIE -> "IMAGE_UI_FEATURE_UNLOCK_FEATURE_KEY_ART_PENNYS_PURSUIT";
            case BEGHOULED -> "IMAGE_UI_FEATURE_UNLOCK_FEATURE_KEY_ART_EVENTS";
            case ZOMBOTANY -> "IMAGE_UI_FEATURE_UNLOCK_FEATURE_KEY_ART_ARENA";
        };
    }
}
