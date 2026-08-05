package io.java.pvz.utils;

import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.models.entities.plants.Plant;


public class PlantCardButton extends Table {
    private final Image bgImage;
    private final Image plantImage;
    private final Image familyImage;
    private final Plant plant;

    public PlantCardButton(Image bgImage, Image plantImage, Image familyImage, Plant plant) {
        this.bgImage = bgImage;
        this.plantImage = plantImage;
        this.familyImage = familyImage;
        this.plant = plant;

        buildUI();
    }

    private void buildUI() {
        setBackground(bgImage.getDrawable());

        plantImage.setScaling(Scaling.fit);
        familyImage.setScaling(Scaling.fit);

        add(plantImage).size(105, 105).center();

        addActor(familyImage);
    }

    @Override
    public void layout() {
        super.layout();

        float iconSize = 32f;

        float padding = -5f;

        familyImage.setSize(iconSize, iconSize);
        familyImage.setPosition(padding, getHeight() - iconSize - padding);
    }

}
