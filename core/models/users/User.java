package models.users;

import com.fasterxml.jackson.annotation.*;
import models.entities.plants.Plant;
import models.entities.plants.PlantSaveData;
import models.entities.zombies.Zombie;
import models.entities.zombies.ZombieSaveData;
import models.enums.Gender;
import models.enums.SecurityQuestion;
import models.game.minigame.MiniGameType;
import models.greenhouse.GreenHouse;
import models.quest.QuestManager;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
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
    private int highestUnlockedChapterIndex;
    private int highestUnlockedLevelIndex;
    private Map<MiniGameType, Integer> unlockedMinigames = new EnumMap<>(MiniGameType.class);

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
        this.highestUnlockedChapterIndex = 0; //start with Egypt
        this.highestUnlockedLevelIndex = 0;
        for (MiniGameType type : MiniGameType.values())
            this.unlockedMinigames.put(type, 0);

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
                @JsonProperty("questManager") QuestManager questManager,
                @JsonProperty("highestUnlockedChapterIndex") int highestUnlockedChapterIndex,
                @JsonProperty("highestUnlockedLevelIndex") int highestUnlockedLevelIndex,
                @JsonProperty("unlockedMinigames") Map<MiniGameType, Integer> unlockedMinigames) {


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
        this.highestUnlockedChapterIndex = highestUnlockedChapterIndex;
        this.highestUnlockedLevelIndex = highestUnlockedLevelIndex;
        this.unlockedMinigames = (unlockedMinigames != null) ? unlockedMinigames : new EnumMap<>(MiniGameType.class);
        if (this.unlockedMinigames.isEmpty())
            for (MiniGameType type : MiniGameType.values())
                this.unlockedMinigames.put(type, 0);
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

    @JsonIgnore
    public ArrayList<Zombie> getUnlockedZombies() {
        return unlockedZombies;
    }

    @JsonIgnore
    public void setUnlockedZombies(ArrayList<Zombie> zombies) {
        if (zombies != null) {
            this.unlockedZombies.clear();
            this.unlockedZombies.addAll(zombies);
        }
    }

    @JsonIgnore
    public ArrayList<Plant> getUnlockedPlants() {
        return unlockedPlants;
    }

    @JsonIgnore
    public void setUnlockedPlants(ArrayList<Plant> plants) {
        if (plants != null) {
            this.unlockedPlants.clear();
            this.unlockedPlants.addAll(plants);
        }
    }

    @JsonProperty("unlockedZombiesData")
    public ArrayList<ZombieSaveData> getUnlockedZombiesData() {
        ArrayList<ZombieSaveData> data = new ArrayList<>();
        for (Zombie zombie : unlockedZombies) {
            data.add(new ZombieSaveData(zombie.getType()));
        }
        return data;
    }

    @JsonProperty("unlockedZombiesData")
    public void setUnlockedZombiesData(ArrayList<ZombieSaveData> dataList) {
        this.unlockedZombies.clear();
        if (dataList != null) {
            for (ZombieSaveData data : dataList) {
                Zombie rebuiltZombie = models.entities.zombies.ZombieFactory.create(data.getType(),-1);
                if (rebuiltZombie != null) {
                    this.unlockedZombies.add(rebuiltZombie);
                }
            }
        }
    }

    @JsonProperty("unlockedPlantsData")
    public ArrayList<PlantSaveData> getUnlockedPlantsData() {
        ArrayList<PlantSaveData> data = new ArrayList<>();
        for (Plant plant : unlockedPlants) {
            data.add(new PlantSaveData(plant.getId(), plant.getLevel(), plant.isBoosted()));
        }
        return data;
    }

    @JsonProperty("unlockedPlantsData")
    public void setUnlockedPlantsData(ArrayList<PlantSaveData> dataList) {
        this.unlockedPlants.clear();
        if (dataList != null) {
            for (PlantSaveData data : dataList) {
                Plant rebuiltPlant = models.entities.plants.PlantFactory.create(data.getId());
                if (rebuiltPlant != null) {
                    rebuiltPlant.setBoosted(data.isBoosted());

                    while (rebuiltPlant.getLevel() < data.getLevel()) {
                        rebuiltPlant.upgrade();
                    }
                    this.unlockedPlants.add(rebuiltPlant);
                }
            }
        }
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public void setQuestManager(QuestManager questManager) {
        this.questManager = questManager;
    }

    public int getHighestUnlockedChapterIndex() {
        return highestUnlockedChapterIndex;
    }

    public void setHighestUnlockedChapterIndex(int index) {
        this.highestUnlockedChapterIndex = index;
    }

    public int getHighestUnlockedLevelIndex() {
        return highestUnlockedLevelIndex;
    }

    public void setHighestUnlockedLevelIndex(int index) {
        this.highestUnlockedLevelIndex = index;
    }


    public Map<MiniGameType, Integer> getUnlockedMinigames() {
        return unlockedMinigames;
    }

    @JsonSetter
    public void setUnlockedMinigames(Map<MiniGameType, Integer> unlockedMinigames) {
        if (unlockedMinigames != null)
            this.unlockedMinigames = unlockedMinigames;
    }

    public int getUnlockedLevelInMinigame(MiniGameType type) {
        return unlockedMinigames.getOrDefault(type, 1);
    }

    public void unlockNextLevelInMinigame(MiniGameType type) {
        int currentUnlocked = getUnlockedLevelInMinigame(type);
        if (currentUnlocked < 2)
            unlockedMinigames.put(type, currentUnlocked + 1);
    }

}