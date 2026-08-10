package io.java.pvz.utils;

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
    private final Image boostedBgImage;
    private final Image plantImage;

    private final Image familyImage;
    private final Plant plant;

    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final Stack progressStack;

    private final Container<Label> levelContainer;

    private boolean isReadyToUpgrade;
    private boolean isUnlocked;
    private boolean boosted = false;
    private Image darkOverlay;
    private Image lockIcon;

    private PlantCardButton(Builder builder) {
        this.boostedBgImage = UiFactory.imageFor(AssetLoader.getInstance().getTextures(), "IMAGE_UI_PACKETS_BOOST");
        this.bgImage = (boosted) ? boostedBgImage : builder.bgImage;
        this.plantImage = builder.plantImage;
        this.familyImage = builder.familyImage;
        this.plant = builder.plant;

        User user = App.getActiveUser();
        this.isUnlocked = user.isItUnlocked(plant);
        if(builder.showLevel)
            builder.showLevel = isUnlocked;

        if (builder.showProgressBar) {
            Skin skin = builder.skin;
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

            this.isReadyToUpgrade = seedPacketCost <= amount;
        } else {
            this.progressBar = null;
            this.progressLabel = null;
            this.progressStack = null;
            this.isReadyToUpgrade = false;
        }

        if (builder.showLevel && builder.skin != null) {
            Label lbl = new Label("LVL " + plant.getLevel(), builder.skin,"medium_outline");
            lbl.setFontScale(1f);
            lbl.setAlignment(Align.center);

            this.levelContainer = new Container<>(lbl);
            this.levelContainer.setTransform(true);
            this.levelContainer.setOrigin(Align.center);
            this.levelContainer.setRotation(-40);
        } else {
            this.levelContainer = null;
        }

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

        if (familyImage != null) {
            familyImage.setScaling(Scaling.fit);
        }

        add(plantImage).size(105, 105).expand().center().row();

        if (progressStack != null) {
            add(progressStack).fillX().height(18).bottom();
        }

        if (familyImage != null) {
            addActor(familyImage);
        }

        if (levelContainer != null) {
            addActor(levelContainer);
        }

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

        if (familyImage != null) {
            familyImage.setSize(iconSize, iconSize);
            familyImage.setPosition(padding, getHeight() - iconSize - padding);
        }

        if (levelContainer != null) {
            levelContainer.pack();
            float padX = 8f;
            float padY = 8f;
            levelContainer.setPosition(getWidth() - levelContainer.getWidth() , getHeight() - levelContainer.getHeight() +30f);
        }

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

        if (progressBar != null) {
            int amount = user.getInventory().getSeedPackets().getOrDefault(plant.getName(), 0);
            setProgress(amount);
            this.isReadyToUpgrade = progressBar.getMaxValue() <= amount;
        }

        if (levelContainer != null) {
            levelContainer.getActor().setText("LVL " + plant.getLevel());
            levelContainer.pack();
        }

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

    public void setBoosted(boolean boosted) {
        this.boosted = boosted;
        setBackground(boostedBgImage.getDrawable());
    }

    public static class Builder {
        private Image bgImage;
        private Image plantImage;
        private Image familyImage;
        private Plant plant;
        private Skin skin;
        private boolean showProgressBar = true;

        private boolean showLevel = true;

        public Builder setBgImage(Image bgImage) {
            this.bgImage = bgImage;
            return this;
        }

        public Builder setPlantImage(Image plantImage) {
            this.plantImage = plantImage;
            return this;
        }

        public Builder setFamilyImage(Image familyImage) {
            this.familyImage = familyImage;
            return this;
        }

        public Builder setPlant(Plant plant) {
            this.plant = plant;
            return this;
        }

        public Builder setSkin(Skin skin) {
            this.skin = skin;
            return this;
        }

        public Builder setShowProgressBar(boolean showProgressBar) {
            this.showProgressBar = showProgressBar;
            return this;
        }

        public Builder setShowLevel(boolean showLevel) {
            this.showLevel = showLevel;
            return this;
        }

        public PlantCardButton build() {
            if (bgImage == null || plantImage == null || plant == null) {
                throw new IllegalStateException("Not all the necessary arguments are set!");
            }

            if ((showProgressBar || showLevel) && skin == null) {
                throw new IllegalStateException("Skin is not set!");
            }
            return new PlantCardButton(this);
        }
    }

    public Plant getPlant() {
        return plant;
    }
}
