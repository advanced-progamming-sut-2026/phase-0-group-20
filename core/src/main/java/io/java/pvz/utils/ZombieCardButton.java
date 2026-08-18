package io.java.pvz.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.models.entities.zombies.Zombie;

public class ZombieCardButton extends Group {
    private static final float PADDING = 4;
    private final Skin skin;
    private final Zombie zombie;
    private final Label costLabel;

    public ZombieCardButton(Image background, Image zombieImage, Zombie zombie , Skin skin ) {
        this.zombie = zombie;
        this.skin  = skin;
        setSize(background.getWidth(), background.getHeight());

        background.setPosition(0, 0);
        addActor(background);

        if (zombieImage != null) {
            zombieImage.setPosition(
                (getWidth() - zombieImage.getWidth()) / 2f,
                (getHeight() - zombieImage.getHeight()) / 2f
            );
            addActor(zombieImage);
            costLabel = createCostLabel(zombie.getWaveCost());
            addActor(costLabel);
            positionCostLabel();
        }else costLabel = null;



        setTouchable(Touchable.enabled);
    }

    private Label createCostLabel(int cost) {

        Label label = new Label(String.valueOf(cost),skin,"medium_outline" );
        label.setAlignment(Align.center);
        label.setFontScale(1.1f);
        label.pack();
        label.setSize(label.getWidth() + PADDING * 2f, label.getHeight() + PADDING);
        return label;
    }

    private void positionCostLabel() {
        costLabel.setPosition(
            getWidth() - costLabel.getWidth() - PADDING,
            PADDING
        );
    }


    public Zombie getZombie() {
        return zombie;
    }

    @Override
    public void setSize(float width, float height) {
        super.setSize(width, height);
        if (costLabel != null) {
            positionCostLabel();
        }
    }
}
