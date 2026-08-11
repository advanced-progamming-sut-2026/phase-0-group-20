package io.java.pvz.utils;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TiledDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.models.entities.plants.Plant;
import pvz.libpvz.textures.TextureBank;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConveyorBeltUI extends Table {
    private final Skin skin;
    private final TextureBank textures;

    private final Group cardsArea;
    private final Map<Plant, PlantCardButton> cardMap = new HashMap<>();
    private final Function<Plant, PlantCardButton> cardFactory;

    public ConveyorBeltUI(Skin skin, TextureBank textures, Function<Plant, PlantCardButton> cardFactory) {
        super();
        this.skin = skin;
        this.textures = textures;
        this.cardFactory = cardFactory;
        this.cardsArea = new Group();

        buildUI();
    }

    private void buildUI() {
        Image edgeLeft = UiFactory.imageFor(textures, Ids.UI.CONVEYOR_SIDE);
        Image edgeRight = UiFactory.imageFor(textures, Ids.UI.CONVEYOR_SIDE);
        Image top = UiFactory.imageFor(textures, Ids.UI.CONVEYOR_TOP);
        Image filling = UiFactory.imageFor(textures, Ids.UI.CONVEYOR_PANEL);
        this.pad(0);
        this.defaults().space(0);

        edgeLeft.setScaling(Scaling.stretch);
        edgeRight.setScaling(Scaling.stretch);
        top.setScaling(Scaling.stretchX);

        TextureRegionDrawable fillingRegion = (TextureRegionDrawable) filling.getDrawable();
        TiledDrawable tiledFilling = new TiledDrawable(fillingRegion.getRegion());

        Table middleSection = new Table();
        middleSection.setBackground(tiledFilling);

        middleSection.add(top).growX().height(15).top().row();

        middleSection.add(cardsArea).grow().padTop(10).padBottom(10);

        add(edgeLeft).width(12).fillY().padRight(-5f);
        add(middleSection).growX().fillY();
        add(edgeRight).width(12).fillY().padLeft(-5f);
    }


    public void updateConveyor(float delta, List<Plant> currentConveyorPlants) {
        if (currentConveyorPlants == null) return;
        Iterator<Map.Entry<Plant, PlantCardButton>> iterator = cardMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Plant, PlantCardButton> entry = iterator.next();
            if (!currentConveyorPlants.contains(entry.getKey())) {
                entry.getValue().remove();
                iterator.remove();
            }
        }

        for (Plant plant : currentConveyorPlants) {
            if (!cardMap.containsKey(plant)) {
                PlantCardButton newCard = cardFactory.apply(plant);

                if (newCard != null) {
                    newCard.setSize(150f, 50f);
                    newCard.setPosition(15f, 0f);

                    cardsArea.addActor(newCard);
                    cardMap.put(plant, newCard);
                }
            }
        }

        float areaHeight = cardsArea.getHeight();
        if (areaHeight <= 0)
            areaHeight = 600f;

        float cardHeight = 50f;
        float padding = 10f;
        float speed = areaHeight / 5.5f;
        float padToTop = 20f;

        for (int i = 0; i < currentConveyorPlants.size(); i++) {
            Plant plant = currentConveyorPlants.get(i);
            PlantCardButton card = cardMap.get(plant);

            if (card != null) {
                float targetY = areaHeight - ((i + 1) * (cardHeight + padding)) + padToTop;

                if (card.getY() < targetY) {
                    float newY = card.getY() + (speed * delta);
                    if (newY > targetY) newY = targetY;
                    card.setY(newY);
                }
            }
        }

    }
}
