package io.java.pvz.views.screens.gameflow;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import io.java.pvz.utils.PamAnimatedActor;
import pvz.libpvz.pam.PamPlayer;

import java.util.List;

public class DetachedPartActor extends Actor {

    private static final float GRAVITY = -1100f;
    private static final float BOUNCE_DAMPING = 0.32f;
    private static final float FRICTION = 0.55f;
    private static final float SETTLE_VELOCITY = 45f;
    private static final float LINGER_SECONDS = 1.5f;
    private static final float FADE_SECONDS = 0.4f;

    private final PamPlayer player;
    private final String pamPath;
    private final String clip;
    private final float frozenTime;
    private final List<String> partNames;
    private final float facing;

    private float vx, vy, angularVel;
    private float rotation = 0f;
    private final float groundY;
    private boolean settled = false;
    private float settleTimer = 0f;
    private boolean fadeStarted = false;

    public DetachedPartActor(PamPlayer player, String pamPath, String clip, float frozenTime,
                             List<String> partNames, float startX, float startY, float groundY,
                             float facing) {
        this.player = player;
        this.pamPath = pamPath;
        this.clip = clip;
        this.frozenTime = frozenTime;
        this.partNames = partNames;
        this.groundY = groundY;
        this.facing = facing;

        setPosition(startX, startY);

        this.vx = facing * MathUtils.random(40f, 140f) * (MathUtils.randomBoolean() ? 1f : -0.4f);
        this.vy = MathUtils.random(180f, 340f);
        this.angularVel = MathUtils.random(-420f, 420f) * (MathUtils.randomBoolean() ? 1f : -1f);
    }

    public static DetachedPartActor spawnFrom(PamAnimatedActor source, List<String> partNames, float groundY) {
        if (partNames == null || partNames.isEmpty()) return null;

        PamPlayer player = source.getPlayer();
        String pamPath = source.getPamPath();
        String clip = source.getClip();
        float time = source.getStateTime();
        float facing = source.getScaleX() == 0 ? 1f : source.getScaleX();

        float drawX = source.getDrawX();
        float drawY = source.getDrawY();

        Rectangle partRect = player.partBounds(pamPath, clip, time, partNames.get(0));

        float startX;
        float startY;
        if (partRect != null) {
            float localOffsetX = partRect.x + partRect.width / 2f;
            float localOffsetY = partRect.y + partRect.height / 2f;
            startX = drawX + facing * localOffsetX;
            startY = drawY - localOffsetY;
        } else {
            startX = drawX;
            startY = drawY + source.getHeight() / 2f;
        }

        return new DetachedPartActor(player, pamPath, clip, time, partNames, startX, startY, groundY, facing);
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (!settled) {
            vy += GRAVITY * delta;
            moveBy(vx * delta, vy * delta);
            rotation += angularVel * delta;

            if (getY() <= groundY) {
                setY(groundY);
                vy = -vy * BOUNCE_DAMPING;
                vx *= FRICTION;
                angularVel *= FRICTION;

                if (Math.abs(vy) < SETTLE_VELOCITY) {
                    settled = true;
                    vx = 0f;
                    vy = 0f;
                    angularVel *= 0.2f;
                }
            }
        } else {
            if (Math.abs(angularVel) > 1f) {
                rotation += angularVel * delta;
                angularVel *= Math.max(0f, 1f - 6f * delta);
            } else {
                angularVel = 0f;
            }
            settleTimer += delta;
            if (settleTimer >= LINGER_SECONDS && !fadeStarted) {
                fadeStarted = true;
                addAction(Actions.sequence(Actions.fadeOut(FADE_SECONDS), Actions.removeActor()));
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (player == null || pamPath == null) return;

        Matrix4 original = batch.getTransformMatrix().cpy();
        com.badlogic.gdx.graphics.Color originalColor = batch.getColor().cpy();

        Matrix4 rotated = original.cpy()
            .translate(getX(), getY(), 0)
            .rotate(0, 0, 1, rotation)
            .translate(-getX(), -getY(), 0);

        batch.setTransformMatrix(rotated);
        batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);

        try {
            for (String part : partNames) {
                player.drawPart(batch, pamPath, clip, frozenTime, getX(), getY(), part);
            }
        } catch (Exception e) {
            System.err.println("❌ Rendering error for detached part " + partNames + ": " + e.getMessage());
        } finally {
            batch.setTransformMatrix(original);
            batch.setColor(originalColor);
        }
    }
}
