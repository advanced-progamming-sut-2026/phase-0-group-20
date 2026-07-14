package models.users;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.Gender;
import models.enums.SecurityQuestion;
import models.greenhouse.GreenHouse;
import models.quest.QuestManager;

import java.util.ArrayList;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private final String id;
    private final ArrayList<Zombie> unlockedZombies = new ArrayList<>();
    private final ArrayList<Plant> unlockedPlants = new ArrayList<>();
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private Gender gender;
    private SecurityQuestion securityQuestion;
    private String securityAnswerHash;
    private int coin;
    private int diamond;
    private int gamesPlayed;
    private int levelsCompleted;
    private boolean stayLoggedIn;
    private Inventory inventory;
    private GreenHouse greenHouse;
    private QuestManager questManager;

    public User(String username, String passwordHash,
                String nickname, String email, Gender gender,
                SecurityQuestion securityQuestion, String securityAnswerHash) {

        this.id = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
        this.coin = 0;
        this.diamond = 0;
        this.gamesPlayed = 0;
        this.levelsCompleted = 0;
        this.stayLoggedIn = false;
        this.inventory = new Inventory();
        this.greenHouse = new GreenHouse();
        this.questManager = new QuestManager();

    }

    @JsonCreator
    public User(@JsonProperty("id") String id,
                @JsonProperty("username") String username,
                @JsonProperty("passwordHash") String passwordHash,
                @JsonProperty("nickname") String nickname,
                @JsonProperty("email") String email,
                @JsonProperty("gender") Gender gender,
                @JsonProperty("inventory") Inventory inventory,
                @JsonProperty("securityQuestion") SecurityQuestion securityQuestion,
                @JsonProperty("securityAnswerHash") String securityAnswerHash,
                @JsonProperty("coin") int coin,
                @JsonProperty("diamond") int diamond,
                @JsonProperty("gamesPlayed") int gamesPlayed,
                @JsonProperty("levelsCompleted") int levelsCompleted,
                @JsonProperty("stayLoggedIn") boolean stayLoggedIn,
                @JsonProperty("greenHouse") GreenHouse greenHouse,
                @JsonProperty("questManager") QuestManager questManager) {

        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
        this.securityQuestion = securityQuestion;
        this.securityAnswerHash = securityAnswerHash;
        this.coin = coin;
        this.diamond = diamond;
        this.gamesPlayed = gamesPlayed;
        this.levelsCompleted = levelsCompleted;
        this.stayLoggedIn = stayLoggedIn;
        this.greenHouse = (greenHouse != null) ? greenHouse : new GreenHouse();
        this.questManager = (questManager != null) ? questManager : new QuestManager();
        this.inventory = (inventory != null) ? inventory : new Inventory();
    }

    public String getId() {
        return id;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public SecurityQuestion getSecurityQuestion() {
        return securityQuestion;
    }

    public void setSecurityQuestion(SecurityQuestion securityQuestion) {
        this.securityQuestion = securityQuestion;
    }

    public String getSecurityAnswerHash() {
        return securityAnswerHash;
    }

    public void setSecurityAnswerHash(String securityAnswerHash) {
        this.securityAnswerHash = securityAnswerHash;
    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public void costCoin(int amount) {
        this.coin = Math.max(0, this.coin - amount);
    }

    public void earnCoin(int amount) {
        this.coin += amount;
    }

    public int getDiamond() {
        return diamond;
    }

    public void setDiamond(int diamond) {
        this.diamond = diamond;
    }

    public void costDiamond(int amount) {
        this.diamond = Math.max(0, this.diamond - amount);
    }

    public void earnDiamond(int amount) {
        this.diamond += amount;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public void setGamesPlayed(int gamesPlayed) {
        this.gamesPlayed = gamesPlayed;
    }

    public int getLevelsCompleted() {
        return levelsCompleted;
    }

    public void setLevelsCompleted(int levelsCompleted) {
        this.levelsCompleted = levelsCompleted;
    }

    public boolean isStayLoggedIn() {
        return stayLoggedIn;
    }

    public void setStayLoggedIn(boolean stayLoggedIn) {
        this.stayLoggedIn = stayLoggedIn;
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

    public ArrayList<Zombie> getUnlockedZombies() {
        return unlockedZombies;
    }

    @JsonSetter
    public void setUnlockedZombies(ArrayList<Zombie> zombies) {
        if (zombies != null) {
            this.unlockedZombies.clear();
            this.unlockedZombies.addAll(zombies);
        }
    }

    public ArrayList<Plant> getUnlockedPlants() {
        return unlockedPlants;
    }

    @JsonSetter
    public void setUnlockedPlants(ArrayList<Plant> plants) {
        if (plants != null) {
            this.unlockedPlants.clear();
            this.unlockedPlants.addAll(plants);
        }
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public void setQuestManager(QuestManager questManager) {
        this.questManager = questManager;
    }
}