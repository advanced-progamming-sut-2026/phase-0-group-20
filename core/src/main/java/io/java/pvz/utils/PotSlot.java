package io.java.pvz.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.java.pvz.controllers.GameController.GreenHouseController;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.greenhouse.GreenHouse;
import io.java.pvz.models.greenhouse.Pot;
import io.java.pvz.models.greenhouse.PotCondition;
import pvz.libpvz.textures.TextureBank;

public class PotSlot extends Stack {
    private static final int BUY_PRICE = 10;
    private static final int GROW_PRICE = 10;
    private final Pot pot;
    private Label timeLabel;
    private final GreenHouseController controller;
    private GreenHouse greenHouse;
    private final int row;
    private final int column;

    public PotSlot(Pot pot, TextureBank textures, int row, int column, GreenHouseController controller,
                   GreenHouse greenHouse) {
        this.pot = pot;
        this.controller = controller;
        this.greenHouse = greenHouse;

        this.row = row;
        this.column = column;

        setSize(120, 120);

        update(textures);
        this.setTouchable(Touchable.enabled);
    }

    public void update(TextureBank textures) {
        this.clearChildren();

        Skin skin = AssetLoader.getInstance().getSkin();
        PotCondition condition = pot.getPotCondition();

        if (condition != PotCondition.LOCKED) {
            setupVaseImage(textures);
        }

        switch (condition) {
            case PLANTED, COLLECTABLE -> buildPlantedOrCollectableUI(textures, skin, condition);
            case LOCKED -> buildLockedUI(textures, skin);
        }
    }

    private void setupVaseImage(TextureBank textures) {
        Image vase = UiFactory.imageFor(textures, Ids.ZenGarden.VASE);
        Container<Image> vaseContainer = new Container<>(vase);
        vaseContainer.size(100, 95);
        this.add(vaseContainer);
    }

    private void buildPlantedOrCollectableUI(TextureBank textures, Skin skin, PotCondition condition) {
        Plant plant = pot.getPlantedPlant();
        String atlasName = (plant == null) ? "MARIGOLD" : UiFactory.getAnimationName(plant);
        PamAnimatedActor actor = PamAnimatedActor.createPlantIdle(atlasName);
        actor.setScale(0.7f);

        Container<PamAnimatedActor> actorContainer = new Container<>(actor);
        actorContainer.size(80, 80);
        actorContainer.padBottom(120);
        this.add(actorContainer);

        if (condition == PotCondition.PLANTED) {
            buildTimerAndSpeedUpOverlay(textures, skin);
        }
    }

    private void buildTimerAndSpeedUpOverlay(TextureBank textures, Skin skin) {
        Table bottomOverlay = new Table();
        bottomOverlay.bottom().center();

        Table timeTable = new Table();
        timeLabel = new Label(pot.getFormattedRemainingTime(), skin, "FBUSV8C5EI_1_outline", Color.WHITE);
        timeLabel.setFontScale(0.85f);
        timeTable.add(timeLabel).center();

        TextButton gemButton = new TextButton(String.valueOf(GROW_PRICE), skin, "purple");
        gemButton.getLabel().setFontScale(0.9f);

        Image gemIcon = UiFactory.imageFor(textures, Ids.ZenGarden.DIAMOND);
        if (gemIcon != null) {
            gemButton.clearChildren();
            gemButton.add(gemIcon).size(20, 20).padRight(3);
            gemButton.add(gemButton.getLabel());
        }

        gemButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.grow(row, column, greenHouse);
                update(textures);
            }
        });

        bottomOverlay.add(timeTable).bottom().padRight(-4f);
        bottomOverlay.add(gemButton).size(70, 38).bottom();

        Container<Table> overlayContainer = new Container<>(bottomOverlay);
        overlayContainer.setFillParent(true);
        overlayContainer.bottom().center();
        overlayContainer.padBottom(10);
        bottomOverlay.padTop(100);

        this.add(overlayContainer);
    }

    private void buildLockedUI(TextureBank textures, Skin skin) {
        boolean isFirstLocked = checkIsFirstLocked();

        if (isFirstLocked) {
            buildBuyButton(textures, skin);
        } else {
            buildLockIcon(textures);
        }
    }

    private void buildBuyButton(TextureBank textures, Skin skin) {
        Button.ButtonStyle buyStyle = new Button.ButtonStyle();
        buyStyle.up = UiFactory.imageFor(textures, Ids.ZenGarden.UNCLICKED_BUY_LABEL).getDrawable();
        buyStyle.down = UiFactory.imageFor(textures, Ids.ZenGarden.CLICKED_BUY_LABEL).getDrawable();
        Button buyButton = new Button(buyStyle);

        Label priceLabel = new Label(String.valueOf(BUY_PRICE), skin, "FBUSV8C5EI_1_outline", Color.WHITE);
        priceLabel.setFontScale(1f);
        buyButton.add(priceLabel).padBottom(10).padLeft(30);

        buyButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                controller.buyPot(pot);
                refreshAllSlots(textures);
            }
        });

        Container<Button> buyContainer = new Container<>(buyButton);
        buyContainer.size(200, 70);
        buyContainer.center();

        this.add(buyContainer);
    }

    private void buildLockIcon(TextureBank textures) {
        Image lock = UiFactory.imageFor(textures, Ids.ZenGarden.LOCK);
        Container<Image> lockContainer = new Container<>(lock);
        lockContainer.size(70, 80);
        this.add(lockContainer);
    }

    private void refreshAllSlots(TextureBank textures) {
        if (getParent() != null) {
            for (Actor child : getParent().getChildren()) {
                if (child instanceof PotSlot) {
                    ((PotSlot) child).update(textures);
                }
            }
        }
    }

    private boolean checkIsFirstLocked() {
        for (int r = 0; r < GreenHouse.getROW(); r++) {
            for (int c = 0; c < GreenHouse.getCOL(); c++) {
                if (greenHouse.getSpecificPot(r, c).getPotCondition() == PotCondition.LOCKED) {
                    return (r == this.row && c == this.column);
                }
            }
        }
        return false;
    }

    public Pot getPot() {
        return pot;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        if (pot.getPotCondition() == PotCondition.PLANTED && timeLabel != null) {
            timeLabel.setText(pot.getFormattedRemainingTime());
        }
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
