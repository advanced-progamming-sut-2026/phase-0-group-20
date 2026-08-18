package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.models.game.GameSession;
import pvz.skin.BorderedTable;

public class LevelIntroModalTable extends BorderedTable {

    private Actor blocker;

    public LevelIntroModalTable(Skin skin) {
        super();
        this.setTouchable(Touchable.enabled);

        pad(40, 50, 40, 50);

        buildContent(skin);

        setSize(600,450);
    }

    private void buildContent(Skin skin) {
        Table contentTable = new Table();

        if (GameSession.getInstance() != null && GameSession.getInstance().getCurrentMode() != null) {
            String modeDetails = GameSession.getInstance().getCurrentMode().toString();
            String[] parts = modeDetails.split("-");

            for (String part : parts) {
                Label label = new Label(part.trim(), skin , "bundle_reward_multiplier");
                label.setFontScale(1.5f);
                label.setAlignment(Align.center);
                label.setColor(Color.WHITE);
                contentTable.add(label).padBottom(15f).row();
            }
        }

        TextButton startBtn = new TextButton("LET'S ROCK!", skin,"green");
        startBtn.getLabel().setFontScale(1.2f);
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove();

                if (GameSession.getInstance() != null) {
                    GameSession.getInstance().resumeGame();
                }
            }
        });

        contentTable.add(startBtn).size(250f, 70f).padTop(30f);

        this.add(contentTable).center();
    }

    public void show(Group targetLayer, Viewport viewport) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        Table blockerTable = new Table();
        blockerTable.setSize(width, height);
        blockerTable.setTouchable(Touchable.enabled);

        blockerTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                return true;
            }
        });

        this.blocker = blockerTable;
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
