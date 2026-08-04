package io.java.pvz.controllers;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.utils.Align;

public final class ButtonAnimator {

    private ButtonAnimator() {}

    public interface OnClickListener {
        void onClick();
    }

    public static void applyHoverAndClickEffect(Actor actor, float hoverScale,
                                                float clickScale, OnClickListener listener) {
        actor.setOrigin(Align.center);

        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(hoverScale, hoverScale, 0.15f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(1f, 1f, 0.15f));
                }
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                actor.clearActions();
                actor.addAction(Actions.scaleTo(clickScale, clickScale, 0.05f));
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                actor.clearActions();
                if (actor.hit(x, y, true) != null) {
                    actor.addAction(Actions.scaleTo(hoverScale, hoverScale, 0.1f));
                    if (listener != null) {
                        listener.onClick();
                    }
                } else {
                    actor.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
    }
}
