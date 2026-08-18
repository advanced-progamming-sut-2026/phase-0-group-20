package io.java.pvz.views.screens.modals;

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
import io.java.pvz.utils.UiFactory;
import pvz.skin.BorderedTable;

public class MatchFoundTable extends BorderedTable {

    private Table blocker;

    public MatchFoundTable(Skin skin, String opponentUsername, String role) {
        super();
        pad(40);
        setSize(700, 380);

        Label title = new Label("Opponent Found!", skin, "big");
        title.setColor(Color.valueOf("#4A3018"));
        title.setFontScale(1.6f);
        title.setAlignment(Align.center);
        add(title).padBottom(25).row();

        String opponentText = (opponentUsername == null || opponentUsername.isBlank())
            ? "Selecting..." : opponentUsername;

        Label info = new Label(
            "Your Opponent: " + opponentText + "\n" + "Your Role: " + role,
            skin);
        info.setColor(Color.valueOf("#4A3018"));
        info.setFontScale(1.3f);
        info.setAlignment(Align.center);
        info.setWrap(true);
        add(info).width(620).padBottom(15).row();

        Label hint = new Label("Match will start soon...", skin);
        hint.setColor(Color.valueOf("#6b5535"));
        hint.setFontScale(1.05f);
        hint.setAlignment(Align.center);
        add(hint).padBottom(30).row();

        TextButton okBtn = UiFactory.textButton("Ok", skin, "purple", 1.05f, 0.95f, this::remove);
        okBtn.getLabel().setFontScale(1.15f);
        add(okBtn).width(200).height(65);
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

        setPosition(Math.round((width - getWidth()) / 2f), Math.round((height - getHeight()) / 2f));
        targetLayer.addActor(this);
    }

    @Override
    public boolean remove() {
        if (blocker != null) blocker.remove();
        return super.remove();
    }
}
