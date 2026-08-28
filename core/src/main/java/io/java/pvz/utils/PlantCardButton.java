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
    private final Container<Label> costContainer;

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
        if (builder.showLevel) {
            builder.showLevel = isUnlocked;
        }

        this.progressBar = createProgressBar(builder, user);
        this.progressLabel = createProgressLabel(builder, user, this.progressBar);
        this.progressStack = createProgressStack(this.progressBar, this.progressLabel);
        this.isReadyToUpgrade = checkReadyToUpgrade(builder, user);

        this.levelContainer = createLevelContainer(builder);
        this.costContainer = createCostContainer(builder);

        initLockOverlay(builder);
        buildUI(builder);
    }

    private ProgressBar createProgressBar(Builder builder, User user) {
        if (!builder.showProgressBar) {
            return null;
        }
        int mathPower = (int) Math.pow(2, plant.getLevel());
        int seedPacketCost = BASE_SEED_PACKETS * mathPower;
        ProgressBar bar = new ProgressBar(0, seedPacketCost, 1, false, builder.skin, "xp_yellow");
        bar.setValue(user.getInventory().getSeedPackets().getOrDefault(plant.getName(), 0));
        return bar;
    }

    private Label createProgressLabel(Builder builder, User user, ProgressBar bar) {
        if (!builder.showProgressBar || bar == null) {
            return null;
        }
        int amount = user.getInventory().getSeedPackets().getOrDefault(plant.getName(), 0);
        Label label = new Label(amount + "/" + (int) bar.getMaxValue(), builder.skin);
        label.setAlignment(Align.center);
        label.setFontScale(0.7f);
        return label;
    }

    private Stack createProgressStack(ProgressBar bar, Label label) {
        if (bar == null || label == null) {
            return null;
        }
        Stack stack = new Stack();
        stack.add(bar);
        stack.add(label);
        return stack;
    }

    private boolean checkReadyToUpgrade(Builder builder, User user) {
        if (!builder.showProgressBar) {
            return false;
        }
        int mathPower = (int) Math.pow(2, plant.getLevel());
        int seedPacketCost = BASE_SEED_PACKETS * mathPower;
        int amount = user.getInventory().getSeedPackets().getOrDefault(plant.getName(), 0);
        return seedPacketCost <= amount;
    }

    private Container<Label> createLevelContainer(Builder builder) {
        if (!builder.showLevel || builder.skin == null) {
            return null;
        }
        Label lbl = new Label("LVL " + plant.getLevel(), builder.skin, "medium_outline");
        lbl.setFontScale(1f);
        lbl.setAlignment(Align.center);

        Container<Label> container = new Container<>(lbl);
        container.setTransform(true);
        container.setOrigin(Align.center);
        container.setRotation(-40);
        return container;
    }

    private Container<Label> createCostContainer(Builder builder) {
        if (!builder.costIncluded || builder.skin == null) {
            return null;
        }
        int cost = (builder.cost == -1) ? plant.getCost() : builder.cost;
        Label costLbl = new Label(String.valueOf(cost), builder.skin, "medium_outline");
        costLbl.setFontScale(1.3f);
        costLbl.setAlignment(Align.center);

        Container<Label> container = new Container<>(costLbl);
        container.setTransform(true);
        container.setOrigin(Align.center);
        return container;
    }

    private void initLockOverlay(Builder builder) {
        if (!isUnlocked && builder.lockIncluded) {
            Pixmap dimPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            dimPixmap.setColor(0, 0, 0, 0.65f);
            dimPixmap.fill();
            darkOverlay = new Image(new Texture(dimPixmap));
            dimPixmap.dispose();

            TextureBank textures = AssetLoader.getInstance().getTextures();
            lockIcon = UiFactory.imageFor(textures, "IMAGE_ZEN_GARDEN_LOCKED_POT_ICON");
        }
    }

    private void buildUI(Builder builder) {
        setBackground(bgImage.getDrawable());
        plantImage.setScaling(Scaling.fit);

        if (familyImage != null) {
            familyImage.setScaling(Scaling.fit);
        }

        add(plantImage).size(builder.scale, builder.scale).expand().center().row();

        if (progressStack != null) {
            add(progressStack).fillX().height(18).bottom();
        }

        if (familyImage != null) {
            addActor(familyImage);
        }

        if (levelContainer != null) {
            addActor(levelContainer);
        }

        if (costContainer != null) {
            addActor(costContainer);
        }

        if (!isUnlocked && builder.lockIncluded) {
            addActor(darkOverlay);
            if (lockIcon != null) {
                addActor(lockIcon);
            }
        }
    }

    @Override
    public void layout() {
        super.layout();
        layoutFamilyImage();
        layoutLevelContainer();
        layoutCostContainer();
        layoutLockOverlay();
    }

    private void layoutFamilyImage() {
        if (familyImage != null) {
            float iconSize = 32f;
            float padding = -5f;
            familyImage.setSize(iconSize, iconSize);
            familyImage.setPosition(padding, getHeight() - iconSize - padding);
        }
    }

    private void layoutLevelContainer() {
        if (levelContainer != null) {
            levelContainer.pack();
            levelContainer.setPosition(getWidth() - levelContainer.getWidth(),
                getHeight() - levelContainer.getHeight() + 30f);
        }
    }

    private void layoutCostContainer() {
        if (costContainer != null) {
            costContainer.pack();
            float padX = 8f;
            float padY = (progressStack != null) ? 12 : 8f;
            costContainer.setPosition(getWidth() - costContainer.getWidth() - padX, padY);
        }
    }

    private void layoutLockOverlay() {
        if (!isUnlocked) {
            if (darkOverlay != null) {
                darkOverlay.setSize(getWidth(), getHeight());
                darkOverlay.setPosition(0, 0);
            }
            if (lockIcon != null) {
                lockIcon.setSize(45, 55);
                lockIcon.setPosition((getWidth() - lockIcon.getWidth()) / 2,
                    (getHeight() - lockIcon.getHeight()) / 2);
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

        updateLockOverlayState();
    }

    private void updateLockOverlayState() {
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

    public Plant getPlant() {
        return plant;
    }

    public static class Builder {
        private Image bgImage;
        private Image plantImage;
        private Image familyImage;
        private Plant plant;
        private Skin skin;
        private boolean showProgressBar = true;
        private float scale = 105f;
        private boolean lockIncluded = true;
        private boolean costIncluded = true;
        private int cost = -1;
        private boolean showLevel = true;

        public Builder setBgImage(Image bgImage) { this.bgImage = bgImage; return this; }
        public Builder setPlantImage(Image plantImage) { this.plantImage = plantImage; return this; }
        public Builder setFamilyImage(Image familyImage) { this.familyImage = familyImage; return this; }
        public Builder setPlant(Plant plant) { this.plant = plant; return this; }
        public Builder setSkin(Skin skin) { this.skin = skin; return this; }
        public Builder setShowProgressBar(boolean show) { this.showProgressBar = show; return this; }
        public Builder setSize(float scale) { this.scale = scale; return this; }
        public Builder setShowLevel(boolean show) { this.showLevel = show; return this; }
        public Builder setLockIncluded(boolean included) { this.lockIncluded = included; return this; }
        public Builder setCostIncluded(boolean included) { this.costIncluded = included; return this; }
        public Builder setCost(int cost) { this.cost = cost; return this; }

        public PlantCardButton build() {
            if (bgImage == null || plantImage == null || plant == null) {
                throw new IllegalStateException("Not all the necessary arguments are set!");
            }
            if ((showProgressBar || showLevel || costIncluded) && skin == null) {
                throw new IllegalStateException("Skin is not set!");
            }
            return new PlantCardButton(this);
        }
    }
}
