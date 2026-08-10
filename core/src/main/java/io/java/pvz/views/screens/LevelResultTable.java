package io.java.pvz.views.screens;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.models.enums.GameState;
import io.java.pvz.models.game.GameSession;
import io.java.pvz.utils.UiFactory;
import pvz.skin.BorderedTable;

public class LevelResultTable extends BorderedTable {

    private Table blocker;

    public LevelResultTable(Skin skin, GameState result) {
        super();
        pad(35, 45, 35, 45);
        setSize(700, 420);
        buildContent(skin, result);
    }

    private void buildContent(Skin skin, GameState result) {
        boolean won = result == GameState.WON;

        Label titleLabel = new Label(won ? "LEVEL COMPLETE!" : "GAME OVER", skin, "big");
        titleLabel.setColor(won ? Color.valueOf("#2ECC71") : Color.valueOf("#E74C3C"));
        titleLabel.setFontScale(2f);
        titleLabel.setAlignment(Align.center);
        add(titleLabel).padBottom(20).row();

        Label subLabel = new Label(
            won ? "You survived every wave!" : "The zombies ate your brains...",
            skin
        );
        subLabel.setColor(Color.valueOf("#4A3018"));
        subLabel.setFontScale(1.2f);
        subLabel.setAlignment(Align.center);
        add(subLabel).padBottom(40).row();

        TextButton continueBtn = UiFactory.textButton(
            won ? "Continue" : "Try Again Later",
            skin, "green_small", 1.05f, 0.95f,
            () -> {
                GameSession.destroyInstance();
                remove();
                ScreenManager.getInstance().popScreen();
                ScreenManager.getInstance().popScreen();
            }
        );
        continueBtn.getLabel().setFontScale(1.3f);
        add(continueBtn).size(320, 80);
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
        if (blocker != null) {
            blocker.remove();
        }
        return super.remove();
    }
}
