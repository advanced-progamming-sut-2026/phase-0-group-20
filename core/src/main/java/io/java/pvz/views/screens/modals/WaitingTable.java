package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.utils.UiFactory;
import pvz.skin.BorderedTable;

public class WaitingTable extends BorderedTable {

    private final Label statusLabel;
    private Table blocker;
    private Runnable onCancel;

    public WaitingTable(Skin skin, String initialMessage) {
        super();
        pad(40);
        setSize(650, 320);

        Label title = new Label("Connecting To Server...", skin, "big");
        title.setColor(Color.valueOf("#4A3018"));
        title.setFontScale(1.4f);
        title.setAlignment(Align.center);
        add(title).padBottom(20).row();

        statusLabel = new Label(initialMessage, skin);
        statusLabel.setColor(Color.valueOf("#4A3018"));
        statusLabel.setFontScale(1.2f);
        statusLabel.setAlignment(Align.center);
        statusLabel.setWrap(true);
        add(statusLabel).width(560).padBottom(30).row();

        TextButton cancelBtn = UiFactory.textButton("Cancel", skin, "brown", 1.05f, 0.95f, () -> {
            if (onCancel != null) onCancel.run();
            remove();
        });
        cancelBtn.getLabel().setFontScale(1.1f);
        add(cancelBtn).width(180).height(60);
    }

    public void setOnCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }

    public void setStatus(String message) {
        statusLabel.setText(message);
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
