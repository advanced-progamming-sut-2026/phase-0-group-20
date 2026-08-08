package io.java.pvz.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.users.User;
import pvz.libpvz.textures.TextureBank;

public class PlantCardButton extends Table {
    private static final int BASE_SEED_PACKETS = 10;
    private final Image bgImage;
    private final Image plantImage;
    private final Image familyImage;
    private final Plant plant;
    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final Stack progressStack;

    private boolean isReadyToUpgrade;
    private boolean isUnlocked;

    private Image darkOverlay;
    private Image lockIcon;

    public PlantCardButton(Image bgImage, Image plantImage, Image familyImage, Plant plant, Skin skin) {
        this.bgImage = bgImage;
        this.plantImage = plantImage;
        this.familyImage = familyImage;
        this.plant = plant;

        User user = App.getActiveUser();
        this.isUnlocked = user.isItUnlocked(plant);

        int mathPower = (int) Math.pow(2, plant.getLevel());
        int seedPacketCost = BASE_SEED_PACKETS * mathPower;
        this.progressBar = new ProgressBar(0, seedPacketCost, 1, false, skin, "xp_yellow");

        int amount = user.getInventory().getSeedPackets().getOrDefault(plant.getName(), 0);
        this.progressBar.setValue(amount);

        this.progressLabel = new Label(amount + "/" + seedPacketCost, skin);
        this.progressLabel.setAlignment(Align.center);
        this.progressLabel.setFontScale(0.7f);

        this.progressStack = new Stack();
        this.progressStack.add(this.progressBar);
        this.progressStack.add(this.progressLabel);

        isReadyToUpgrade = seedPacketCost <= amount;

        if (!isUnlocked) {
            Pixmap dimPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            dimPixmap.setColor(0, 0, 0, 0.65f);
            dimPixmap.fill();
            darkOverlay = new Image(new Texture(dimPixmap));
            dimPixmap.dispose();

            TextureBank textures = AssetLoader.getInstance().getTextures();
            lockIcon = UiFactory.imageFor(textures, "IMAGE_ZEN_GARDEN_LOCKED_POT_ICON");
        }

        buildUI();
    }

    private void buildUI() {
        setBackground(bgImage.getDrawable());

        plantImage.setScaling(Scaling.fit);
        familyImage.setScaling(Scaling.fit);

        add(plantImage).size(105, 105).expand().center().row();
        add(progressStack).fillX().height(18).bottom();

        addActor(familyImage);

        if (!isUnlocked) {
            addActor(darkOverlay);
            if (lockIcon != null) {
                addActor(lockIcon);
            }
        }
    }

    @Override
    public void layout() {
        super.layout();

        float iconSize = 32f;
        float padding = -5f;

        familyImage.setSize(iconSize, iconSize);
        familyImage.setPosition(padding, getHeight() - iconSize - padding);

        if (!isUnlocked) {
            if (darkOverlay != null) {
                darkOverlay.setSize(getWidth(), getHeight());
                darkOverlay.setPosition(0, 0);
            }

            if (lockIcon != null) {
                lockIcon.setSize(45, 55);
                lockIcon.setPosition((getWidth() - lockIcon.getWidth()) / 2, (getHeight() - lockIcon.getHeight()) / 2);
            }
        }
    }

    public void updateState() {
        User user = App.getActiveUser();

        this.isUnlocked = user.getUnlockedPlants().contains(plant);

        int amount = user.getInventory().getSeedPackets().getOrDefault(plant.getName(), 0);
        setProgress(amount);

        this.isReadyToUpgrade = progressBar.getMaxValue() <= amount;

        if (this.isUnlocked) {
            if (darkOverlay != null) {
                darkOverlay.remove();
                darkOverlay = null;
            }
            if (lockIcon != null) {
                lockIcon.remove();
                lockIcon = null;
            }
        }
    }

    public void setProgress(int amount) {
        if (progressBar != null && progressLabel != null) {
            progressBar.setValue(amount);
            int max = (int) progressBar.getMaxValue();
            progressLabel.setText(amount + "/" + max);
        }
    }

    public boolean isReadyToUpgrade() {
        return isReadyToUpgrade;
    }

    public boolean isUnlocked() {
        return isUnlocked;
    }
}
