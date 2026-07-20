package controllers.GameController;

import models.App;
import models.InGameEntityGenerator;
import models.Result;
import models.entities.plants.Plant;
import models.entities.zombies.Zombie;
import models.enums.Menu;
import models.game.Arena;
import models.game.GameSession;
import models.game.adventure.Adventure;
import models.game.adventure.levels.Level;
import models.timeManager.TimeManager;
import models.users.User;

import java.util.ArrayList;
import java.util.List;

public class PlantSelectionController {
    private final List<Plant> selectedPlants = new ArrayList<>();
    private final List<String> boostedPlantNames = new ArrayList<>();


    public Result showAllPlants() {
        User activeUser = App.getActiveUser();
        if (activeUser == null) return new Result(false, "No active user!");

        List<Plant> unlocked = activeUser.getUnlockedPlants();
        if (unlocked == null || unlocked.isEmpty()) {
            return new Result(true, "You don't have any plants yet!");
        }

        StringBuilder sb = new StringBuilder("--- Your Unlocked Plants ---\n");
        for (Plant p : unlocked) {
            sb.append("- ").append(p.getName()).append(" (Cost: ").append(p.getCost()).append(")\n");
        }
        return new Result(true, sb.toString().trim());
    }

    public Result showAllAvailablePlants() {
        if (selectedPlants.isEmpty()) {
            return new Result(true, "You haven't selected any plants yet.");
        }

        StringBuilder sb = new StringBuilder("--- Selected Plants for this Level ---\n");
        for (Plant p : selectedPlants) {
            sb.append("- ").append(p.getName());
            if (boostedPlantNames.contains(p.getName().toLowerCase())) {
                sb.append(" [BOOSTED]");
            }
            sb.append("\n");
        }
        return new Result(true, sb.toString().trim());
    }

    public Result addPlant(String name) {
        User activeUser = App.getActiveUser();
        Level currentLevel = App.getActiveAdventure().getCurrentChapter().getCurrentLevel();

        if (selectedPlants.size() >= currentLevel.getPlantSlotCount()) {
            return new Result(false, "Your seed slots are full! (" + currentLevel.getPlantSlotCount() + " plants max)");
        }

        for (Plant p : selectedPlants) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                return new Result(false, "You have already selected this plant.");
            }
        }

        for (Plant p : activeUser.getUnlockedPlants()) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                selectedPlants.add(p);
                return new Result(true, p.getName() + " added to your selection.");
            }
        }

        return new Result(false, "You don't own a plant named " + name + "!");
    }

    public Result removePlant(String name) {
        for (Plant p : selectedPlants) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                selectedPlants.remove(p);
                boostedPlantNames.remove(p.getName().toLowerCase());
                return new Result(true, p.getName() + " removed from your selection.");
            }
        }
        return new Result(false, "Plant not found in your current selection.");
    }

    public Result boostPlant(String name) {
        User activeUser = App.getActiveUser();

        boolean found = false;
        for (Plant p : selectedPlants) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                found = true;
                break;
            }
        }

        if (!found) {
            return new Result(false, "You must add the plant to your selection first before boosting it!");
        }

        if (boostedPlantNames.contains(name.trim().toLowerCase())) {
            return new Result(false, "This plant is already boosted for this level!");
        }

        if (activeUser.getDiamond() >= 2) { // cost for boost (it can be different)
            activeUser.earnDiamond(-2);
            boostedPlantNames.add(name.trim().toLowerCase());
            return new Result(true, name + " is BOOSTED for the upcoming level! (-2 Diamonds)");
        } else {
            return new Result(false, "Not enough diamonds to boost this plant! (Requires 2)");
        }
    }

    public Result startGame() {
        if (selectedPlants.isEmpty()) {
            return new Result(false, "You must select at least one plant to start the game!");
        }

        for (String pName : boostedPlantNames) {
            Plant p = getPlantByName(pName);
            if (p != null) {
                p.setBoosted(true);
            }
        }

        List<Plant> inGamePlants = new ArrayList<>();
        for (Plant p : selectedPlants) {
            inGamePlants.add(InGameEntityGenerator.getPlantForGame(p, p.isBoosted()));
        }

        GameSession.startNewGame(inGamePlants);

        App.setActiveMenu(Menu.GAME_FLOW_MENU);
        selectedPlants.clear();
        boostedPlantNames.clear();

        return new Result(true, "Game Started! Good luck defending your brains!");
    }


    private Plant getPlantByName(String name) {
        for (Plant p : selectedPlants) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                return p;
            }
        }
        return null;
    }

}
