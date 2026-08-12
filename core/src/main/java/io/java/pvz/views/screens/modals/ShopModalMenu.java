package io.java.pvz.views.screens.modals;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.java.pvz.controllers.ButtonAnimator;
import io.java.pvz.controllers.GameController.ShopMenuController;
import io.java.pvz.controllers.GameMenuController;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.Shop;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.users.User;
import io.java.pvz.utils.Ids;
import io.java.pvz.utils.UiFactory;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.BorderedTable;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ShopModalMenu extends Table {
    private static final Color BROWN = Color.valueOf("#4A3018");
    private static final Color LIGHT_BROWN = Color.valueOf("#8B5A2B");
    private static final Color SUCCESS_COLOR = Color.valueOf("#2ECC71");
    private static final Color ERROR_COLOR = Color.valueOf("#E74C3C");

    private static final float CARD_WIDTH = 250f;
    private static final float CARD_HEIGHT = 360f;

    private final ShopMenuController controller = new ShopMenuController();
    private final GameMenuController menuController;
    private final TextureBank textures;
    private final Skin skin;

    private Table dailyDealSlot;
    private Table itemsRow;
    private Label statusLabel;

    private int selectivePlantIndex = 0;
    private Group modalLayer; // Added to hold the blocker and confirm dialog

    public static Table build(GameMenuController menuController, TextureBank textures, Skin skin, Group modalLayer) {
        return new ShopModalMenu(menuController, textures, skin, modalLayer);
    }

    private ShopModalMenu(GameMenuController menuController, TextureBank textures, Skin skin, Group modalLayer) {
        this.menuController = menuController;
        this.textures = textures;
        this.skin = skin;
        this.modalLayer = modalLayer;
        setFillParent(true);
        this.setTouchable(Touchable.enabled);
        this.addListener(new ClickListener());
        buildUI();
    }

    private void buildUI() {
        BorderedTable card = new BorderedTable();
        card.pad(35, 40, 30, 40);

        Table header = new Table();
        TextButton closeBtn = UiFactory.getCloseBtn(skin, menuController::goBack);

        Image shopIcon = UiFactory.imageFor(textures, Ids.GameScreen.SHOP_ICON);
        shopIcon.setScaling(Scaling.fit);

        Label title = new Label("Crazy Dave's Shop", skin, "big");
        title.setColor(BROWN);
        title.setFontScale(1.5f);
        title.setAlignment(Align.center);

        header.add(closeBtn).size(45, 45).left();
        header.add(shopIcon).size(55, 55).padLeft(15).padRight(10);
        header.add(title).expandX().center().padRight(60);
        card.add(header).growX().padBottom(10).row();

        statusLabel = new Label("", skin);
        statusLabel.setFontScale(1.05f);
        statusLabel.setAlignment(Align.center);
        statusLabel.setWrap(true);
        card.add(statusLabel).growX().padBottom(10).row();

        dailyDealSlot = new Table();
        card.add(dailyDealSlot).growX().padBottom(25).row();

        Label subTitle = new Label("More in the Shop", skin);
        subTitle.setColor(BROWN);
        subTitle.setFontScale(1.6f);
        card.add(subTitle).left().padBottom(12).row();

        itemsRow = new Table();
        itemsRow.left().top();

        ScrollPane scrollPane = new ScrollPane(itemsRow, skin);
        scrollPane.setScrollingDisabled(false, true);
        scrollPane.setFadeScrollBars(true);

        card.add(scrollPane).growX().height(CARD_HEIGHT + 30).row();

        add(card).width(1550).height(830).center();

        refreshAll();
    }

    private void refreshDailyDeal() {
        dailyDealSlot.clear();

        Stack dailyStack = new Stack();

        Image bg = UiFactory.imageFor(textures, Ids.Shop.DAILY_DEAL_BACKGROUND);
        bg.setScaling(Scaling.stretch);
        Container<Image> bgContainer = new Container<>(bg);
        bgContainer.size(1470, 220);
        dailyStack.add(bgContainer);

        Plant dailyPlant = controller.getDailyDealPlant();
        boolean soldOut = controller.isDailyDealPurchased();

        Table content = new Table();
        content.pad(15, 35, 15, 35);

        Image plantIcon = dailyPlant != null
            ? UiFactory.imageFor(textures,
            Ids.Shop.PLANT_PACKET_PREFIX + UiFactory.getAtlasName(dailyPlant).toUpperCase())
            : UiFactory.imageFor(textures, Ids.Shop.SEED_PACKET_ICON);
        plantIcon.setScaling(Scaling.fit);
        content.add(plantIcon).size(150, 150).padRight(30);

        Table info = new Table();
        info.left().defaults().left();

        Table clockRow = new Table();
        Image clock = UiFactory.imageFor(textures, Ids.Shop.DAILY_DEAL_CLOCK_ICON);
        clock.setScaling(Scaling.fit);
        Label dailyLabel = new Label("DAILY DEAL", skin);
        dailyLabel.setColor(Color.valueOf("#FFD37A"));
        dailyLabel.setFontScale(1.3f);
        clockRow.add(clock).size(24, 24).padRight(8);
        clockRow.add(dailyLabel);
        info.add(clockRow).padBottom(10).row();

        String plantName = dailyPlant != null ? dailyPlant.getName() : "No plants unlocked yet";
        Label nameLabel = new Label(dailyPlant != null ? ("10x " + plantName + " Seed Packets") :
            plantName, skin, "big");
        nameLabel.setColor(Color.WHITE);
        nameLabel.setFontScale(1.25f);
        info.add(nameLabel).row();

        content.add(info).expandX().left();

        Image ribbon = UiFactory.imageFor(textures, Ids.Shop.SALE_RIBBON);
        ribbon.setScaling(Scaling.fit);
        content.add(ribbon).size(85, 85).padRight(25);

        Table buySection = new Table();
        if (dailyPlant == null) {
            Label lockedLabel = new Label("Unlock a plant first!", skin, "big");
            lockedLabel.setColor(Color.LIGHT_GRAY);
            buySection.add(lockedLabel);
        } else if (soldOut) {
            Label soldOutLabel = new Label("Sold Out\nToday", skin, "big");
            soldOutLabel.setColor(Color.LIGHT_GRAY);
            soldOutLabel.setAlignment(Align.center);
            buySection.add(soldOutLabel);
        } else {
            Table priceRow = new Table();
            Image coinIcon = UiFactory.imageFor(textures, Ids.Shop.COIN);
            coinIcon.setScaling(Scaling.fit);
            Label priceLabel = new Label("1600", skin, "big");
            priceLabel.setColor(Color.WHITE);
            priceRow.add(coinIcon).size(36, 36).padRight(8);
            priceRow.add(priceLabel);
            buySection.add(priceRow).padBottom(12).row();

            TextButton buyBtn = UiFactory.textButton("Buy", skin, "green_small", 1.1f, 0.9f, () -> {
                showConfirmDialog(() -> controller.buyItem("daily deal", 1, null), 1600, Shop.CurrencyType.COIN);
            });
            buySection.add(buyBtn).width(170).height(55);
        }
        content.add(buySection).width(220);

        dailyStack.add(content);
        dailyDealSlot.add(dailyStack).width(1470).height(220);
    }

    private void refreshItemsRow() {
        itemsRow.clear();
        itemsRow.defaults().padRight(25).top();

        itemsRow.add(buildFixedItemCard(
            Ids.Shop.CARD_GREEN, Ids.GameScreen.GREENHOUSE_ICON,
            "Pot", "Unlocks a Greenhouse pot (max 20)", 2000, Shop.CurrencyType.COIN,
            () -> showConfirmDialog(() -> controller.buyItem("pot", 1, null), 2000, Shop.CurrencyType.COIN)
        )).size(CARD_WIDTH, CARD_HEIGHT);

        itemsRow.add(buildFixedItemCard(
            Ids.Shop.CARD_BLUE, Ids.Shop.PLANT_FOOD_ICON,
            "Plant Food", "Instantly powers up a plant (max 3 held)", 3, Shop.CurrencyType.DIAMOND,
            () -> showConfirmDialog(() -> controller.buyItem("plant food", 1, null), 3, Shop.CurrencyType.DIAMOND)
        )).size(CARD_WIDTH, CARD_HEIGHT);

        itemsRow.add(buildFixedItemCard(
            Ids.Shop.CARD_YELLOW, Ids.Shop.SEED_PACKET_ICON,
            "Random Seed Packet", "5x seed packets of a random unlocked plant", 1000, Shop.CurrencyType.COIN,
            () -> showConfirmDialog(() -> controller.buyItem("random seed packet",
                1, null), 1000, Shop.CurrencyType.COIN)
        )).size(CARD_WIDTH, CARD_HEIGHT);

        itemsRow.add(buildSelectiveSeedPacketCard()).size(CARD_WIDTH, CARD_HEIGHT);

        itemsRow.add(buildFixedItemCard(
            Ids.Shop.CARD_COIN, Ids.Shop.COIN,
            "Currency Exchange", "Trade 5 diamonds for 500 coins", 5, Shop.CurrencyType.DIAMOND,
            () -> showConfirmDialog(() -> controller.buyItem("currency exchange",
                1, null), 5, Shop.CurrencyType.DIAMOND)
        )).size(CARD_WIDTH, CARD_HEIGHT);

        Map<String, Shop.PlantPrice> plants = controller.getAvailablePlants();
        for (Map.Entry<String, Shop.PlantPrice> entry : plants.entrySet()) {
            itemsRow.add(buildPlantCard(entry.getKey(), entry.getValue())).size(CARD_WIDTH, CARD_HEIGHT);
        }
    }

    private Table buildFixedItemCard(String bgImageId, String iconId, String title, String description,
                                     int amount, Shop.CurrencyType currency, Runnable onBuy) {
        Table card = new Table();
        card.pad(18);
        card.setBackground(UiFactory.imageFor(textures, bgImageId).getDrawable());

        Image icon = UiFactory.imageFor(textures, iconId);
        icon.setScaling(Scaling.fit);
        card.add(icon).size(95, 95).padTop(8).padBottom(12).row();

        Label titleLabel = new Label(title, skin, "big");
        titleLabel.setColor(BROWN);
        titleLabel.setFontScale(1f);
        titleLabel.setWrap(true);
        titleLabel.setAlignment(Align.center);
        card.add(titleLabel).width(200).padBottom(6).row();

        Label descLabel = new Label(description, skin);
        descLabel.setColor(LIGHT_BROWN);
        descLabel.setFontScale(0.85f);
        descLabel.setWrap(true);
        descLabel.setAlignment(Align.center);
        card.add(descLabel).width(200).expandY().top().padBottom(10).row();

        card.add(buildPriceRow(amount, currency)).padBottom(10).row();
        card.add(buildBuyButton(currency, onBuy)).width(150).height(50);

        return card;
    }

    private Table buildSelectiveSeedPacketCard() {
        User user = App.getActiveUser();
        List<Plant> unlocked = user.getUnlockedPlants();

        Table card = new Table();
        card.pad(18);
        card.setBackground(UiFactory.imageFor(textures, Ids.Shop.CARD_PURPLE).getDrawable());

        Image icon = UiFactory.imageFor(textures, Ids.Shop.SELECTIVE_SEED_PACKET_ICON);
        icon.setScaling(Scaling.fit);
        card.add(icon).size(85, 85).padTop(8).padBottom(10).row();

        Label titleLabel = new Label("Selective Seed Packet", skin, "big");
        titleLabel.setColor(BROWN);
        titleLabel.setFontScale(0.9f);
        titleLabel.setWrap(true);
        titleLabel.setAlignment(Align.center);
        card.add(titleLabel).width(200).padBottom(8).row();

        if (unlocked.isEmpty()) {
            Label lockedLabel = new Label("Unlock a plant first!", skin);
            lockedLabel.setColor(LIGHT_BROWN);
            lockedLabel.setFontScale(0.9f);
            lockedLabel.setWrap(true);
            lockedLabel.setAlignment(Align.center);
            card.add(lockedLabel).width(200).expandY().top();
            return card;
        }

        if (selectivePlantIndex >= unlocked.size()) {
            selectivePlantIndex = 0;
        }
        Plant selected = unlocked.get(selectivePlantIndex);

        Table pickerRow = new Table();
        Image leftArrow = UiFactory.imageFor(textures, Ids.Shop.ARROW_LEFT);
        Image rightArrow = UiFactory.imageFor(textures, Ids.Shop.ARROW_RIGHT);
        leftArrow.setScaling(Scaling.fit);
        rightArrow.setScaling(Scaling.fit);
        leftArrow.setTouchable(Touchable.enabled);
        rightArrow.setTouchable(Touchable.enabled);

        ButtonAnimator.applyHoverAndClickEffect(leftArrow, 1.15f, 0.9f, () -> {
            selectivePlantIndex = (selectivePlantIndex - 1 + unlocked.size()) % unlocked.size();
            refreshItemsRow();
        });
        ButtonAnimator.applyHoverAndClickEffect(rightArrow, 1.15f, 0.9f, () -> {
            selectivePlantIndex = (selectivePlantIndex + 1) % unlocked.size();
            refreshItemsRow();
        });

        Label plantNameLabel = new Label(selected.getName(), skin);
        plantNameLabel.setColor(BROWN);
        plantNameLabel.setFontScale(0.9f);
        plantNameLabel.setAlignment(Align.center);
        plantNameLabel.setWrap(true);

        pickerRow.add(leftArrow).size(22, 22).padRight(6);
        pickerRow.add(plantNameLabel).width(110);
        pickerRow.add(rightArrow).size(22, 22).padLeft(6);
        card.add(pickerRow).expandY().top().padBottom(10).row();

        card.add(buildPriceRow(5, Shop.CurrencyType.DIAMOND)).padBottom(10).row();

        String targetPlantName = selected.getName();
        TextButton buyBtn = UiFactory.textButton("Buy 10x", skin, "purple", 1.1f, 0.9f, () -> {
            showConfirmDialog(() -> controller.buyItem("selective seed packet",
                1, targetPlantName), 5, Shop.CurrencyType.DIAMOND);
        });
        card.add(buyBtn).width(150).height(50);

        return card;
    }

    private Table buildPlantCard(String plantName, Shop.PlantPrice price) {
        Plant plant = App.findPlantByName(plantName);

        Table card = new Table();
        card.pad(18);
        card.setBackground(UiFactory.imageFor(textures, Ids.Shop.CARD_PLANT).getDrawable());

        Image icon = plant != null
            ? UiFactory.imageFor(textures, Ids.Shop.PLANT_PACKET_PREFIX + UiFactory.getAtlasName(plant).toUpperCase())
            : UiFactory.imageFor(textures, Ids.Shop.SEED_PACKET_ICON);
        icon.setScaling(Scaling.fit);
        card.add(icon).size(105, 105).padTop(6).padBottom(12).row();

        Label nameLabel = new Label(plantName, skin, "big");
        nameLabel.setColor(BROWN);
        nameLabel.setFontScale(0.95f);
        nameLabel.setWrap(true);
        nameLabel.setAlignment(Align.center);
        card.add(nameLabel).width(200).expandY().top().padBottom(10).row();

        card.add(buildPriceRow(price.amount, price.currency)).padBottom(10).row();
        card.add(buildBuyButton(price.currency, () -> {
            showConfirmDialog(() -> controller.buyPlant(plantName), price.amount, price.currency);
        })).width(150).height(50);

        return card;
    }

    private Table buildPriceRow(int amount, Shop.CurrencyType currency) {
        Table priceRow = new Table();
        String iconId = currency == Shop.CurrencyType.COIN ? Ids.Shop.COIN : Ids.Shop.DIAMOND;
        Image currencyIcon = UiFactory.imageFor(textures, iconId);
        currencyIcon.setScaling(Scaling.fit);
        Label priceLabel = new Label(String.valueOf(amount), skin, "big");
        priceLabel.setColor(BROWN);
        priceRow.add(currencyIcon).size(28, 28).padRight(6);
        priceRow.add(priceLabel);
        return priceRow;
    }

    private TextButton buildBuyButton(Shop.CurrencyType currency, Runnable onBuy) {
        String style = currency == Shop.CurrencyType.DIAMOND ? "purple" : "green_small";
        return UiFactory.textButton("Buy", skin, style, 1.1f, 0.9f, onBuy::run);
    }

    private void refreshAll() {
        refreshDailyDeal();
        refreshItemsRow();
    }

    private void showStatus(Result result) {
        if (result == null) return;
        statusLabel.setText(result.message());
        statusLabel.setColor(result.isSuccessful() ? SUCCESS_COLOR : ERROR_COLOR);
    }

    private void showConfirmDialog(Supplier<Result> onConfirm, int price, Shop.CurrencyType currency) {
        if (modalLayer != null && modalLayer.getStage() != null) {
            float width = modalLayer.getStage().getViewport().getWorldWidth();
            float height = modalLayer.getStage().getViewport().getWorldHeight();

            ShopConfirmTable confirmDialog = new ShopConfirmTable(onConfirm, price, currency, width, height);
            modalLayer.addActor(confirmDialog);
        }
    }

    private class ShopConfirmTable extends Table {
        private final Supplier<Result> onConfirm;
        private Actor blocker;

        public ShopConfirmTable(Supplier<Result> onConfirm, int price, Shop.CurrencyType currency,
                                float worldWidth, float worldHeight) {
            this.onConfirm = onConfirm;
            setFillParent(true);
            buildBlocker(worldWidth, worldHeight);
            buildUi(price, currency);
        }

        private void buildBlocker(float width, float height) {
            Table blockerTable = new Table();
            blockerTable.setSize(width, height);
            blockerTable.setTouchable(Touchable.enabled);
            blockerTable.addListener(new InputListener() {
                @Override
                public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                    return true;
                }
            });
            this.blocker = blockerTable;
            addActor(blockerTable);
        }

        private void buildUi(int price, Shop.CurrencyType currency) {
            BorderedTable dialogBox = new BorderedTable();
            dialogBox.setSize(500, 300);
            dialogBox.pad(30);
            Label title = new Label("Confirm Purchase", skin, "big");
            title.setColor(BROWN);
            String exchangeKey = (currency == Shop.CurrencyType.COIN) ? " coins" : " diamonds";
            Label question = new Label("Do you want to buy this Item for " + price + exchangeKey + "?", skin);
            question.setWrap(true);
            question.setColor(Color.valueOf("#4A3018"));
            question.setAlignment(Align.center);

            Table buttonsRow = new Table();

            TextButton cancelBtn = UiFactory.textButton("Cancel", skin, "brown", 1.1f, 0.9f, this::remove);

            TextButton confirmBtn = UiFactory.textButton("Buy", skin, "green", 1.1f, 0.9f, () -> {
                Result result = onConfirm.get();
                showStatus(result);
                refreshAll();
                this.remove();
            });

            buttonsRow.add(cancelBtn).size(150, 60).padRight(30);
            buttonsRow.add(confirmBtn).size(150, 60);

            dialogBox.add(title).padBottom(20).row();
            dialogBox.add(question).growX().padBottom(30).row();
            dialogBox.add(buttonsRow);

            add(dialogBox).expand().center();
        }

        @Override
        public boolean remove() {
            if (blocker != null) {
                blocker.remove();
            }
            return super.remove();
        }
    }
}
