package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.java.pvz.controllers.GameController.GreenHouseController;
import io.java.pvz.controllers.ScreenManager;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.models.App;
import io.java.pvz.models.greenhouse.GreenHouse;
import io.java.pvz.models.greenhouse.Pot;
import io.java.pvz.models.greenhouse.PotCondition;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.PotSlot;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;

public class ZenGarden extends BaseScreen{
    private final GreenHouseController controller = new GreenHouseController();
    private final GreenHouse greenHouse;
    private final Skin skin;
    private final Texture background;
    public ZenGarden(Game game) {
        super(game);
        greenHouse = App.getActiveUser().getGreenHouse();
        skin = AssetLoader.getInstance().getSkin();
        buildUi();
        this.background = AssetLoader.getInstance().getTexture("background/zen_garden.png");
    }

    private void buildUi() {
        TextureBank textures = AssetLoader.getInstance().getTextures();
        Table topLayer = new Table();
        topLayer.setFillParent(true);
        topLayer.top().left();
        topLayer.add(UiFactory.iconButton(textures, skin, Ids.GameScreen.BACK_ICON, 100, 100,
            () -> ScreenManager.getInstance().popScreen())).pad(20);
        mainLayer.addActor(topLayer);

        Table rootTable = new Table();
        rootTable.setFillParent(true);
        Table gridTable = new Table();
        gridTable.setDebug(true);
        float slotWidth = 195;
        float slotHeight = 190;
        float gapX = 45f;
        float gapY = 42f;

        for (int row = 0; row < GreenHouse.getROW(); row++) {
            for (int col = 0; col < GreenHouse.getCOL(); col++) {

                Pot currentPot = greenHouse.getSpecificPot(row, col);
                PotSlot slotUI = new PotSlot(currentPot, textures , row , col , controller , greenHouse);

                slotUI.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (currentPot.getPotCondition() == PotCondition.EMPTY) {
                            System.out.println("Planting a new seed!");
                            controller.plantPot(slotUI.getRow(),slotUI.getColumn(),greenHouse);
                            slotUI.update(textures);
                        } else if (currentPot.getPotCondition() == PotCondition.COLLECTABLE) {
                            System.out.println("Collecting reward!");
                            controller.collect(slotUI.getRow(),slotUI.getColumn(),greenHouse);
                            slotUI.update(textures);
                        }
                    }
                });

                gridTable.add(slotUI).size(slotWidth, slotHeight).fill().padRight(gapX).padBottom(gapY);
            }
            gridTable.row();
        }

        rootTable.add(gridTable).expand().center().padTop(260).padLeft(40);

        mainLayer.addActor(rootTable);
    }

    @Override
    public void render(float delta) {

        if (background != null) {
            batch.begin();
            batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            batch.end();
        }


        stage.act(delta);
        stage.draw();

    }


}
