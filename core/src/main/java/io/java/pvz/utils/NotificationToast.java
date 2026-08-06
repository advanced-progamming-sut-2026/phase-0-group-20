package io.java.pvz.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.controllers.NotificationManager;

public class NotificationToast extends Group {

    private final Image progressBar;
    private final NotificationManager manager;
    private final float duration = 4f;

    public NotificationToast(String message, Skin skin, NotificationManager manager) {
        this.manager = manager;

        float width = 500f;
        float height = 80f;
        setSize(width, height);

        Drawable bgDrawable = skin.getDrawable("image_ui_if_bundle_reward_multiplier_bg_10");

        Image background = new Image(bgDrawable);
        background.setSize(width, height);
        addActor(background);

        Label textLabel = new Label(message, skin);
        textLabel.setFontScale(1.5f);
        textLabel.setColor(Color.BROWN);
        textLabel.setWrap(true);
        textLabel.setAlignment(Align.center);
        textLabel.setPosition(55, 5);
        textLabel.setSize(width - 70, height - 10);
        addActor(textLabel);

        progressBar = new Image(createColorTexture(Color.valueOf("#C06014")));
        progressBar.setSize(width, 6f);
        progressBar.setPosition(0, 0);
        addActor(progressBar);

        TextButton closeBtn = UiFactory.getCloseBtn(skin, this::dismiss);
        closeBtn.setSize(35, 35);
        closeBtn.setPosition(5, height/2);

        addActor(closeBtn);

        startTimer();    }

    private void startTimer() {
        progressBar.addAction(Actions.sizeTo(0, progressBar.getHeight(), duration, Interpolation.linear));
        this.addAction(Actions.sequence(
            Actions.delay(duration),
            Actions.run(this::dismiss)
        ));
    }

    public void dismiss() {
        this.clearActions();
        this.addAction(Actions.sequence(
            Actions.parallel(
                Actions.moveBy(0, 100, 0.4f, Interpolation.swingIn),
                Actions.fadeOut(0.4f)
            ),
            Actions.run(() -> {
                manager.removeToast(this);
                this.remove();
            })
        ));
    }

    private TextureRegionDrawable createColorTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);
        batch.setColor(Color.WHITE);
    }
}
