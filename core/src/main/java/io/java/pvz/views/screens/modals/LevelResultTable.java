package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.controllers.GameController.GameMenuController;
import io.java.pvz.controllers.GameController.MatchController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.models.Result;
import io.java.pvz.models.enums.GameState;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.models.game.minigame.IZombieLevel;
import io.java.pvz.net.server.PlayerRole;
import io.java.pvz.utils.UiFactory;
import io.java.pvz.views.screens.ChapterSelectionScreen;
import io.java.pvz.views.screens.gameflow.GameFlowScreen;
import pvz.skin.BorderedTable;

public class LevelResultTable extends BorderedTable {

    private Table blocker;
    private Game game;

    public LevelResultTable(Skin skin, GameState result, Game game) {
        super();
        pad(35, 45, 35, 45);
        setSize(700, 420);
        this.game = game;
        buildContent(skin, result);
    }

    private void buildContent(Skin skin, GameState result) {
        boolean won = result == GameState.WON;
        boolean isZombie = isZombieMode();

        String titleText = won ? (isZombie ? "BRAINS ACQUIRED!" : "LEVEL COMPLETE!") : "GAME OVER";
        String subText = won ? (isZombie ? "You ate their brains!" : "You survived every wave!") :
            (isZombie ? "You ran out of time or zombies..." : "The zombies ate your brains...");

        addTitleLabel(titleText, won, skin);
        addSubLabel(subText, skin);
        add(buildButtonsTable(won, skin)).center();
    }

    private boolean isZombieMode() {
        boolean isOnline = MatchController.getInstance().isOnlineMatch();
        boolean isZombieRole = MatchController.getInstance().getCurrentRole() == PlayerRole.ZOMBIE;
        boolean isOfflineZombieMode = !isOnline &&
            (GameSession.getInstance().getCurrentMode() instanceof IZombieLevel);

        return (isOnline && isZombieRole) || isOfflineZombieMode;
    }

    private void addTitleLabel(String text, boolean won, Skin skin) {
        Label titleLabel = new Label(text, skin, "big");
        titleLabel.setColor(won ? Color.valueOf("#2ECC71") : Color.valueOf("#E74C3C"));
        titleLabel.setFontScale(2f);
        titleLabel.setAlignment(Align.center);

        add(titleLabel).padBottom(20).row();
    }

    private void addSubLabel(String text, Skin skin) {
        Label subLabel = new Label(text, skin);
        subLabel.setColor(Color.valueOf("#4A3018"));
        subLabel.setFontScale(1.2f);
        subLabel.setAlignment(Align.center);

        add(subLabel).padBottom(40).row();
    }

    private Table buildButtonsTable(boolean won, Skin skin) {
        Table buttonsTable = new Table();

        if (!won) {
            buttonsTable.add(buildRestartButton(skin)).size(250, 80).padRight(20);
        }

        buttonsTable.add(buildContinueButton(won, skin)).size(250, 80);

        return buttonsTable;
    }

    private TextButton buildRestartButton(Skin skin) {
        TextButton restartBtn = UiFactory.textButton(
            "Restart", skin, "brown", 1.05f, 0.95f, this::handleRestartAction
        );
        restartBtn.getLabel().setFontScale(1.3f);

        return restartBtn;
    }

    private void handleRestartAction() {
        GameFlowController flowController = new GameFlowController();
        Result res = flowController.restartLevel();

        if (res.isSuccessful()) {
            Gdx.app.postRunnable(() -> {
                String mapId = new GameMenuController().getCurrentMapTextureId();
                ScreenManager.getInstance().popScreen();
                ScreenManager.getInstance().pushScreen(new GameFlowScreen(game, mapId));
            });
        }
        remove();
    }

    private TextButton buildContinueButton(boolean won, Skin skin) {
        String buttonText = won ? "Continue" : "Try Again Later";
        TextButton continueBtn = UiFactory.textButton(
            buttonText, skin, "green_small", 1.05f, 0.95f, this::handleContinueAction
        );
        continueBtn.getLabel().setFontScale(1.3f);

        return continueBtn;
    }

    private void handleContinueAction() {
        GameSession.destroyInstance();
        remove();
        ScreenManager.getInstance().popScreen();
        ScreenManager.getInstance().popScreen();

        if (ScreenManager.getInstance().getCurrentScreen() instanceof ChapterSelectionScreen) {
            ScreenManager.getInstance().popScreen();
            ScreenManager.getInstance().pushScreen(new ChapterSelectionScreen(game));
        }
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
}
