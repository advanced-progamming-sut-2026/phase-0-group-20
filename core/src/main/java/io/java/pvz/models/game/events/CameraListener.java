package io.java.pvz.models.game.events;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class CameraListener implements GameEventListener {

    private final Camera camera;
    private final Stage stage;

    private float origCamX = -1;
    private float origCamY = -1;
    private Action currentShakeAction;

    public CameraListener(Camera camera, Stage stage) {
        this.camera = camera;
        this.stage = stage;
    }

    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        switch (event) {
            case GARGANTUAR_MOVES -> {
                shakeCamera(0.2f, 4f);
            }
            case PLANT_EXPLODED ->
                shakeCamera(0.2f, 4f);
            case LAWNMOWER_TRIGGERED -> {
                shakeCamera(0.2f, 4f);
            }

        }
    }

    private void shakeCamera(float duration, float intensity) {
        if (currentShakeAction != null) {
            stage.getRoot().removeAction(currentShakeAction);
        } else {
            origCamX = camera.position.x;
            origCamY = camera.position.y;
        }

        currentShakeAction = new Action() {
            float time = 0;

            @Override
            public boolean act(float delta) {
                time += delta;

                if (time >= duration) {
                    camera.position.set(origCamX, origCamY, camera.position.z);
                    camera.update();
                    currentShakeAction = null;
                    return true;
                }

                float offsetX = MathUtils.random(-intensity, intensity);
                float offsetY = MathUtils.random(-intensity, intensity);

                camera.position.set(origCamX + offsetX, origCamY + offsetY, camera.position.z);
                camera.update();

                return false;
            }
        };

        stage.getRoot().addAction(currentShakeAction);
    }
}
