package io.java.pvz.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.models.App;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.quest.Quest;
import io.java.pvz.models.quest.reward.SeedPackReward;
import io.java.pvz.models.quest.reward.UnlockableReward;
import pvz.libpvz.textures.TextureBank;

public class QuestItemUi extends Table {

    public QuestItemUi(Quest quest, Skin skin, TextureBank textures) {

        Stack stack = new Stack();

        Image cardBg = UiFactory.imageFor(textures, "IMAGE_UI_JOUST_MATCHLOADING_PLAYERPANEL_BG");
        cardBg.setScaling(Scaling.stretch);
        stack.add(cardBg);

        Table contentTable = new Table();
        contentTable.pad(12, 20, 12, 20);

        Table iconFrame = new Table();
        if (quest.isCompleted()) {
            iconFrame.setBackground(
                UiFactory.imageFor(textures, "IMAGE_UI_QUESTS_TRAVEL_LOG_PANEL_EPIC_COMPLETE").getDrawable());
        }
        Image icon = UiFactory.imageFor(textures, iconKeyFor(quest));
        iconFrame.add(icon).size(58, 58);
        contentTable.add(iconFrame).size(66, 66).padRight(18);

        Table middleTable = new Table();
        middleTable.top().left();

        Label titleLabel = new Label(quest.getTitle(), skin, "FBUSV8C5EI_1", Color.BROWN);
        titleLabel.setFontScale(1.0f);

        Label descriptionLabel = new Label(quest.getDescription(), skin, "FBUSV8C5EI_2", Color.DARK_GRAY);
        descriptionLabel.setFontScale(0.82f);
        descriptionLabel.setWrap(true);

        float currentProgress = quest.getCondition().getCurrentProgress();
        float maxProgress = quest.getCondition().getTargetProgress();
        if(maxProgress == 0 )maxProgress = 1.0f;
        ProgressBar progressBar = new ProgressBar(0, maxProgress, 1, false, skin, "xp_green");
        progressBar.setValue(currentProgress);
        progressBar.setAnimateDuration(0.2f);

        Label progressLabel = new Label(
            (int) currentProgress + "/" + (int) maxProgress, skin, "medium_outline");
        progressLabel.setFontScale(0.7f);
        progressLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        Stack progressStack = new Stack();
        progressStack.add(progressBar);
        progressStack.add(progressLabel);

        middleTable.add(titleLabel).left().padBottom(12).row();
        middleTable.add(descriptionLabel).width(320).left().padBottom(14).row();
        middleTable.add(progressStack).width(320).height(22).left();

        contentTable.add(middleTable).expandX().fillX();

        Table rightTable = new Table();

        Table rewardTable = new Table();
        rewardTable.pad(4, 10, 4, 10);
        Image rewardIcon = UiFactory.imageFor(textures, rewardIconKeyFor(quest));
        Label rewardAmount = new Label(getRewardText(quest), skin, "medium_outline");
        rewardAmount.setFontScale(1.0f);

        rewardTable.add(rewardIcon).size(50, 50).padRight(6);
        rewardTable.add(rewardAmount);

        TextButton actionButton;
        if (quest.isReadyToClaim()) {
            actionButton = new TextButton("CLAIM", skin, "green-small");
            actionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    quest.complete();
                }
            });
        } else {
            actionButton = new TextButton("PLAY", skin, "purple");
            actionButton.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                        new GameEventPayload.Builder(GameEvent.NOTIFY)
                            .message("Go Play Some Adventure")
                            .build());
                }
            });
        }

        actionButton.getLabel().setFontScale(1.0f);

        rightTable.add(rewardTable).padRight(16);
        rightTable.add(actionButton).size(104, 44).padRight(20);

        contentTable.add(rightTable);

        stack.add(contentTable);

        this.add(stack).expand().fill();
    }

    private String iconKeyFor(Quest quest) {
        return switch (quest.getCategory()) {
            case DAILY -> "IMAGE_UI_QUESTS_QUESTICONS_ZOMBIE";
            case MAIN -> "IMAGE_UI_QUESTS_QUESTICONS_PLANT";
            case EPIC -> "IMAGE_UI_QUESTS_QUESTICONS_LOTD";
            default -> "IMAGE_UI_QUESTS_QUESTICONS_POWERUPS";
        };
    }

    private String rewardIconKeyFor(Quest quest) {
        switch (quest.getReward().getRewardType()) {
            case CURRENCY:
                return "IMAGE_UI_QUESTS_EPIC_REWARD_COINS";
            case SEED_PACK:
                SeedPackReward seedPackReward = (SeedPackReward) quest.getReward();
                Plant plant = App.findPlantByName(seedPackReward.getPlantTypeName());
                String atlasName = UiFactory.getAtlasName(plant);
                String plantTextureKey = "IMAGE_UI_PACKETS_" + atlasName.toUpperCase();
                return plantTextureKey;
            case UNLOCK_PLANT:
                UnlockableReward unlockableReward = (UnlockableReward) quest.getReward();
                Plant unlockPlant = App.findPlantByName(unlockableReward.getPlantToUnlockName());
                String atlas = UiFactory.getAtlasName(unlockPlant);
                return "IMAGE_UI_PACKETS_" + atlas.toUpperCase();
            case DIAMOND:
            default:
                return "IMAGE_UI_QUESTS_EPIC_REWARD_GEMS";
        }
    }

    private String getRewardText(Quest quest) {
        return switch (quest.getReward().getRewardType()) {
            case DIAMOND, CURRENCY -> "X" + quest.getReward().toString();
            case SEED_PACK -> "X" + (quest.getReward().toString().replace(" seed packets", ""));
            case UNLOCK_PLANT -> quest.getReward().toString();
        };
    }
}
