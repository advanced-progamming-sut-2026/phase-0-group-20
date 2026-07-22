package models.users;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.Gender;
import models.enums.SecurityQuestion;
import models.game.events.GameEvent;
import models.game.events.GameEventMessenger;
import models.game.events.GameEventPayload;
import models.game.minigame.MiniGameType;
import models.greenhouse.GreenHouse;
import models.news.Message;
import models.quest.QuestManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    @JsonProperty("account")
    private UserAccount account;

    @JsonProperty("wallet")
    private UserWallet wallet;

    @JsonProperty("progress")
    private UserProgress progress;

    @JsonProperty("lastLoginEpochDay")
    private long lastLoginEpochDay;

    @JsonProperty("hasPlayedDailyChallengeToday")
    private boolean hasPlayedDailyChallengeToday;

    @JsonProperty("purchasedDailyDealToday")
    private boolean purchasedDailyDealToday;

    @JsonProperty("inventory")
    private Inventory inventory;

    @JsonProperty("greenHouse")
    private GreenHouse greenHouse;

    @JsonProperty("questManager")
    private QuestManager questManager;

    @JsonProperty("inbox")
    private ArrayList<Message> inbox;

    public User(String username, String passwordHash, String nickname, String email,
                Gender gender, SecurityQuestion securityQuestion, String securityAnswerHash) {

        this.account = new UserAccount(username, passwordHash, nickname, email, gender, securityQuestion, securityAnswerHash);
        this.wallet = new UserWallet();
        this.progress = new UserProgress();

        this.lastLoginEpochDay = 0;
        this.hasPlayedDailyChallengeToday = false;
        this.purchasedDailyDealToday = false;

        this.inventory = new Inventory();
        this.greenHouse = new GreenHouse();
        this.questManager = new QuestManager();
        this.inbox = new ArrayList<>();
    }

    @JsonCreator
    public User(@JsonProperty("account") UserAccount account,
                @JsonProperty("wallet") UserWallet wallet,
                @JsonProperty("progress") UserProgress progress,
                @JsonProperty("lastLoginEpochDay") long lastLoginEpochDay,
                @JsonProperty("hasPlayedDailyChallengeToday") boolean hasPlayedDailyChallengeToday,
                @JsonProperty("purchasedDailyDealToday") boolean purchasedDailyDealToday,
                @JsonProperty("inventory") Inventory inventory,
                @JsonProperty("greenHouse") GreenHouse greenHouse,
                @JsonProperty("questManager") QuestManager questManager,
                @JsonProperty("inbox") ArrayList<Message> inbox) {

        this.account = account;
        this.wallet = (wallet != null) ? wallet : new UserWallet();
        this.progress = (progress != null) ? progress : new UserProgress();

        this.lastLoginEpochDay = lastLoginEpochDay;
        this.hasPlayedDailyChallengeToday = hasPlayedDailyChallengeToday;
        this.purchasedDailyDealToday = purchasedDailyDealToday;

        this.inventory = (inventory != null) ? inventory : new Inventory();
        this.greenHouse = (greenHouse != null) ? greenHouse : new GreenHouse();
        this.questManager = (questManager != null) ? questManager : new QuestManager();
        this.inbox = (inbox != null) ? inbox : new ArrayList<>();
    }

    public void performDailyLoginCheck() {
        long currentEpochDay = LocalDate.now().toEpochDay();

        if (this.lastLoginEpochDay == 0) {
            this.lastLoginEpochDay = currentEpochDay;
            return;
        }

        if (currentEpochDay > this.lastLoginEpochDay) {
            int daysPassed = (int) (currentEpochDay - this.lastLoginEpochDay);
            this.lastLoginEpochDay = currentEpochDay;

            GameEventPayload payload = new GameEventPayload.Builder(GameEvent.NEW_DAY_STARTED)
                    .amount(daysPassed)
                    .build();

            GameEventMessenger.getInstance().dispatch(GameEvent.NEW_DAY_STARTED, payload);
        }
    }

    public long getLastLoginEpochDay() {
        return lastLoginEpochDay;
    }

    public void setLastLoginEpochDay(long lastLoginEpochDay) {
        this.lastLoginEpochDay = lastLoginEpochDay;
    }

    public boolean isHasPlayedDailyChallengeToday() {
        return hasPlayedDailyChallengeToday;
    }

    public void setHasPlayedDailyChallengeToday(boolean hasPlayedDailyChallengeToday) {
        this.hasPlayedDailyChallengeToday = hasPlayedDailyChallengeToday;
    }

    public boolean isPurchasedDailyDealToday() {
        return purchasedDailyDealToday;
    }

    public void setPurchasedDailyDealToday(boolean purchasedDailyDealToday) {
        this.purchasedDailyDealToday = purchasedDailyDealToday;
    }


    @JsonIgnore
    public String getId() {
        return account.getId();
    }

    @JsonIgnore
    public String getUsername() {
        return account.getUsername();
    }

    @JsonIgnore
    public void setUsername(String username) {
        account.setUsername(username);
    }

    @JsonIgnore
    public String getPasswordHash() {
        return account.getPasswordHash();
    }

    @JsonIgnore
    public void setPasswordHash(String passwordHash) {
        account.setPasswordHash(passwordHash);
    }

    @JsonIgnore
    public String getNickname() {
        return account.getNickname();
    }

    @JsonIgnore
    public void setNickname(String nickname) {
        account.setNickname(nickname);
    }

    @JsonIgnore
    public String getEmail() {
        return account.getEmail();
    }

    @JsonIgnore
    public void setEmail(String email) {
        account.setEmail(email);
    }

    @JsonIgnore
    public Gender getGender() {
        return account.getGender();
    }

    @JsonIgnore
    public void setGender(Gender gender) {
        account.setGender(gender);
    }

    @JsonIgnore
    public SecurityQuestion getSecurityQuestion() {
        return account.getSecurityQuestion();
    }

    @JsonIgnore
    public void setSecurityQuestion(SecurityQuestion securityQuestion) {
        account.setSecurityQuestion(securityQuestion);
    }

    @JsonIgnore
    public String getSecurityAnswerHash() {
        return account.getSecurityAnswerHash();
    }

    @JsonIgnore
    public void setSecurityAnswerHash(String securityAnswerHash) {
        account.setSecurityAnswerHash(securityAnswerHash);
    }

    @JsonIgnore
    public boolean isStayLoggedIn() {
        return account.isStayLoggedIn();
    }

    @JsonIgnore
    public void setStayLoggedIn(boolean stayLoggedIn) {
        account.setStayLoggedIn(stayLoggedIn);
    }

    @JsonIgnore
    public int getCoin() {
        return wallet.getCoin();
    }

    public void setCoin(int coin) {
        wallet.setCoin(coin);
    }

    public void costCoin(int amount) {
        wallet.costCoin(amount);
    }

    public void earnCoin(int amount) {
        wallet.earnCoin(amount);
    }

    @JsonIgnore
    public int getDiamond() {
        return wallet.getDiamond();
    }

    public void setDiamond(int diamond) {
        wallet.setDiamond(diamond);
    }

    public void costDiamond(int amount) {
        wallet.costDiamond(amount);
    }

    public void earnDiamond(int amount) {
        wallet.earnDiamond(amount);
    }

    @JsonIgnore
    public int getPlantFoodCount() {
        return wallet.getPlantFoodCount();
    }

    public void setPlantFoodCount(int count) {
        wallet.setPlantFoodCount(count);
    }

    public void addPlantFoodCount(int count) {
        wallet.addPlantFoodCount(count);
    }

    @JsonIgnore
    public int getGamesPlayed() {
        return progress.getGamesPlayed();
    }

    public void setGamesPlayed(int gamesPlayed) {
        progress.setGamesPlayed(gamesPlayed);
    }

    @JsonIgnore
    public int getLevelsCompleted() {
        return progress.getLevelsCompleted();
    }

    public void setLevelsCompleted(int levelsCompleted) {
        progress.setLevelsCompleted(levelsCompleted);
    }

    @JsonIgnore
    public int getHighestBonusScore() {
        return progress.getHighestBonusScore();
    }

    public void setHighestBonusScore(int highestBonusScore) {
        progress.setHighestBonusScore(highestBonusScore);
    }

    @JsonIgnore
    public int getHighestUnlockedChapterIndex() {
        return progress.getHighestUnlockedChapterIndex();
    }

    public void setHighestUnlockedChapterIndex(int index) {
        progress.setHighestUnlockedChapterIndex(index);
    }

    @JsonIgnore
    public int getHighestUnlockedLevelIndex() {
        return progress.getHighestUnlockedLevelIndex();
    }

    public void setHighestUnlockedLevelIndex(int index) {
        progress.setHighestUnlockedLevelIndex(index);
    }

    @JsonIgnore
    public ArrayList<Zombie> getUnlockedZombies() {
        return progress.getUnlockedZombies();
    }

    @JsonIgnore
    public ArrayList<Plant> getUnlockedPlants() {
        return progress.getUnlockedPlants();
    }

    @JsonIgnore
    public Map<MiniGameType, Integer> getUnlockedMinigames() {
        return progress.getUnlockedMinigames();
    }

    public int getUnlockedLevelInMinigame(MiniGameType type) {
        return progress.getUnlockedLevelInMinigame(type);
    }

    public void unlockNextLevelInMinigame(MiniGameType type) {
        progress.unlockNextLevelInMinigame(type, this);
    }

    public void addZombiesToUnlock(List<Zombie> inGameZombies) {
        progress.addZombiesToUnlock(inGameZombies, this);
    }

    public void unlockAdventureLevel(int targetChapterIndex, int targetLevelIndex, String chapterName) {
        progress.unlockAdventureLevel(targetChapterIndex, targetLevelIndex, chapterName, this);
    }

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public GreenHouse getGreenHouse() {
        return greenHouse;
    }

    public void setGreenHouse(GreenHouse greenHouse) {
        this.greenHouse = greenHouse;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public void setQuestManager(QuestManager questManager) {
        this.questManager = questManager;
    }

    public ArrayList<Message> getInbox() {
        return inbox;
    }

    public void setInbox(ArrayList<Message> inbox) {
        this.inbox = inbox;
    }

    public void addMessage(Message message) {
        inbox.add(message);
    }
}