package io.java.pvz.views.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.java.pvz.utils.PamAnimatedActor;

public class TransitionScreen extends BaseScreen {
    private final Runnable onComplete;

    public TransitionScreen(Game game, float durationSeconds, Runnable onComplete) {
        super(game);
        this.onComplete = onComplete;

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        Texture bgTexture = new Texture(pixmap);
        pixmap.dispose();

        Image bg = new Image(bgTexture);
        bg.setFillParent(true);
        mainLayer.addActor(bg);

        String pamPath = "768/INITIAL/EFFECTS/LOAD_ICON_BACK/LOAD_ICON_BACK.PAM";
        PamAnimatedActor loadingAnim = PamAnimatedActor.createEffectAnimated(pamPath, "animation");

        String flowerPath = "768/INITIAL/EFFECTS/LOAD_ICON_FRONT/LOAD_ICON_FRONT.PAM";
        PamAnimatedActor flowerAnim = PamAnimatedActor.createEffectAnimated(flowerPath, "animation");

        com.badlogic.gdx.scenes.scene2d.ui.Stack stack = new com.badlogic.gdx.scenes.scene2d.ui.Stack();
        stack.add(loadingAnim);
        stack.add(flowerAnim);
        Table table = new Table();
        table.setFillParent(true);

        table.add(stack).size(150, 150).center();

        mainLayer.addActor(table);
        mainLayer.getColor().a = 0f;
        mainLayer.addAction(Actions.sequence(
            Actions.fadeIn(0.3f),
            Actions.delay(durationSeconds),
            Actions.fadeOut(0.5f),
            Actions.run(() -> {
                if (this.onComplete != null) {
                    this.onComplete.run();
                }
            })
        ));
    }

    @Override
    public void render(float delta) {
        clearScreen(0f, 0f, 0f, 1f);
        if (stage != null) {
            stage.act(delta);
            stage.draw();
        }
    }

    @Override
    protected boolean showsCurrencyBar() {
        return false;
    }
}
