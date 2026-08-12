package io.java.pvz.utils;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import io.java.pvz.controllers.GameController.GameFlowController;
import io.java.pvz.models.App;
import pvz.libpvz.textures.TextureBank;

public class PlantFoodUI extends Group {
    private final Image baseBank;
    private final Image mainLeaf;
    private final Image[] filledSlots;

    public PlantFoodUI(TextureBank textures) {
        super();

        baseBank = UiFactory.imageFor(textures, "IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK");
        if (baseBank != null) {
            addActor(baseBank);
            setSize(baseBank.getWidth(), baseBank.getHeight());
        }

        mainLeaf = UiFactory.imageFor(textures, "IMAGE_EFFECTS_PLANTFOOD_PICKUP_PLANTFOOD_PICKUP_79X79");
        if (mainLeaf != null) {
            float leafOffsetX = 5f;
            float leafOffsetY = 5f;
            mainLeaf.setScale(1.5f);
            mainLeaf.setTouchable(Touchable.enabled);
            mainLeaf.setPosition(leafOffsetX, leafOffsetY);
            mainLeaf.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                   if(App.getActiveUser()!= null){
                       new GameFlowController().cheatAddPlantFood();
                       updateFood(App.getActiveUser().getPlantFoodCount());
                   }
                }
            });
            addActor(mainLeaf);
        }

        filledSlots = new Image[5];

        float firstSlotX = 76f;
        float slotY = 34f;
        float slotSpacing = 24f ;

        for (int i = 0; i < 5; i++) {
            Image slotGlow = UiFactory.imageFor(textures, "IMAGE_UI_HUD_INGAME_PLANTFOOD_BANK_COLLECT");
            slotGlow.setSize(20,20);
            if (slotGlow != null) {
                slotGlow.setPosition(firstSlotX + (i * slotSpacing), slotY);
                slotGlow.setTouchable(Touchable.disabled);
                slotGlow.setVisible(false);

                filledSlots[i] = slotGlow;
                addActor(slotGlow);
            }
        }
    }

    public void updateFood(int currentAmount) {
        for (int i = 0; i < 5; i++) {
            if (filledSlots[i] != null) {
                filledSlots[i].setVisible(i < currentAmount);
            }
        }
    }
}
