package io.java.pvz.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import io.java.pvz.models.entities.zombies.Zombie;

public class ZombieCardButton extends Group {
    private final Zombie zombie;

    public ZombieCardButton(Image background, Image zombieImage, Zombie zombie) {
        this(background, zombieImage, zombie, -1, null);
    }

    public ZombieCardButton(Image background, Image zombieImage, Zombie zombie, int slotNumber, Skin skin) {
        this.zombie = zombie;

        setSize(background.getWidth(), background.getHeight());

        background.setPosition(0, 0);
        addActor(background);

        zombieImage.setPosition(
            (getWidth() - zombieImage.getWidth()) / 2f,
            (getHeight() - zombieImage.getHeight()) / 2f
        );
        addActor(zombieImage);

        if (slotNumber > 0 && skin != null) {
            Label numberLabel = new Label(String.valueOf(slotNumber), skin);
            numberLabel.setFontScale(1.1f);
            numberLabel.setColor(Color.YELLOW);
            numberLabel.setAlignment(Align.center);
            numberLabel.setSize(24f, 24f);
            numberLabel.setPosition(4f, getHeight() - 26f);
            addActor(numberLabel);
        }

        setTouchable(Touchable.enabled);
    }

    public Zombie getZombie() {
        return zombie;
    }
}
