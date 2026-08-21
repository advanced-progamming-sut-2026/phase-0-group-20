package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.java.pvz.loader.AssetLoader;
import io.java.pvz.utils.DialogueLine;
import io.java.pvz.utils.PamAnimatedActor;
import pvz.skin.BorderedTable;

import java.util.List;

public class DialogueModalTable extends Table {

    private static final float CHAR_OFFSET_X = 150f;
    private static final float CHAR_OFFSET_Y = 300f;

    private final Skin skin;
    private final List<DialogueLine> dialogueLines;
    private final Runnable onComplete;
    private int currentIndex = 0;

    private Actor blocker;

    public DialogueModalTable(Skin skin, List<DialogueLine> dialogueLines, Runnable onComplete) {
        super();
        this.skin = skin;
        this.dialogueLines = dialogueLines;
        this.onComplete = onComplete;

        setFillParent(true);

        updateCurrentLine();

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                advanceDialogue();
            }
        });
    }

    private void updateCurrentLine() {
        if (dialogueLines == null || currentIndex >= dialogueLines.size()) return;

        this.clearChildren();

        DialogueLine current = dialogueLines.get(currentIndex);

        Stack layoutStack = new Stack();
        layoutStack.setFillParent(true);

        Table characterLayer = new Table();
        characterLayer.setFillParent(true);

        if (current.getPamPath() != null && !current.getPamPath().isEmpty()) {
            PamAnimatedActor animatedActor = new PamAnimatedActor(
                AssetLoader.getInstance().getPlayer(),
                current.getClipName(),
                current.getPamPath()
            );

            if (current.isLeft()) {
                characterLayer.bottom().left();
                characterLayer.add(animatedActor).padLeft(CHAR_OFFSET_X).padBottom(CHAR_OFFSET_Y);
            } else {
                characterLayer.bottom().right();
                characterLayer.add(animatedActor).padRight(CHAR_OFFSET_X).padBottom(CHAR_OFFSET_Y);
            }
        }

        Table boxLayer = new Table();
        boxLayer.bottom().padBottom(40);

        BorderedTable dialogBox = new BorderedTable();
        dialogBox.pad(20);

        Label speakerLabel = new Label(current.getSpeakerName(), skin, "big");
        speakerLabel.setColor(Color.valueOf("#4A3018"));
        speakerLabel.setAlignment(Align.center);

        Label dialogueTextLabel = new Label(current.getText(), skin, "medium");
        dialogueTextLabel.setColor(Color.BLACK);
        dialogueTextLabel.setWrap(true);
        dialogueTextLabel.setAlignment(Align.center);

        Table textTable = new Table();
        textTable.add(speakerLabel).growX().padBottom(10).row();
        textTable.add(dialogueTextLabel).grow().center();

        dialogBox.add(textTable).grow().center();

        boxLayer.add(dialogBox).width(800).height(180);

        layoutStack.add(characterLayer);
        layoutStack.add(boxLayer);

        this.add(layoutStack).grow();
    }

    private void advanceDialogue() {
        currentIndex++;
        if (currentIndex < dialogueLines.size()) {
            updateCurrentLine();
        } else {
            remove();
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    public void show(Group targetLayer, Viewport viewport) {
        float width = viewport.getWorldWidth();
        float height = viewport.getWorldHeight();

        Table blockerTable = new Table();
        blockerTable.setSize(width, height);
        blockerTable.setTouchable(Touchable.enabled);
        blockerTable.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                advanceDialogue();
                return true;
            }
        });

        this.blocker = blockerTable;
        targetLayer.addActor(blocker);

        setSize(width, height);
        targetLayer.addActor(this);
    }

    @Override
    public boolean remove() {
        if (blocker != null) {
            blocker.remove();
        }
        return super.remove();
    }
}
