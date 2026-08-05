package io.java.pvz.utils;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import io.java.pvz.models.entities.zombies.Zombie;

public class ZombieCardButton extends Group {
    private final Zombie zombie;

    public ZombieCardButton(Image background, Image zombieImage, Zombie zombie) {
        this.zombie = zombie;

        setSize(background.getWidth(), background.getHeight());

        background.setPosition(0, 0);
        addActor(background);

        zombieImage.setPosition(
            (getWidth() - zombieImage.getWidth()) / 2f,
            (getHeight() - zombieImage.getHeight()) / 2f
        );
        addActor(zombieImage);

        setTouchable(Touchable.enabled);
    }

    public Zombie getZombie() {
        return zombie;
    }
}
