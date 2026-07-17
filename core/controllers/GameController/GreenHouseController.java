package controllers.GameController;

import models.App;
import models.Result;
import models.entities.plants.Plant;
import models.greenhouse.GreenHouse;
import models.greenhouse.Pot;
import models.greenhouse.PotCondition;
import models.users.User;

public class GreenHouseController {
    private User user ;

    public Result showGreenHouse(GreenHouse greenHouse) {
        return new Result(true, greenHouse.showGreenHouse());
    }

    public Result plantPot(String x, String y, GreenHouse greenHouse) {
        user = App.getActiveUser();
        int posX, posY;
        try {
            posX = Integer.parseInt(x);
            posY = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        if (posX > greenHouse.getROW() || posY > greenHouse.getCOL()) {
            return new Result(false, "You have a 4 * 5 garden. Where are you looking for?");
        }
        Pot desiredPot = greenHouse.getSpecificPot(posX - 1, posY - 1);
        return switch (desiredPot.getPotCondition()) {
            case LOCKED -> new Result(false, "You haven't unlocked this pot yet.");
            case PLANTED -> new Result(false, "You already planted a plant here.");
            case COLLECTABLE -> new Result(false, "You have to collect the previous plant first.");
            case EMPTY -> {
                desiredPot.plantPlant();
                yield new Result(true, "You planted a new plant here. congrats!!!");
            }
        };


    }

    public Result collect(String x, String y, GreenHouse greenHouse) {
        user = App.getActiveUser();
        int posX, posY;
        try {
            posX = Integer.parseInt(x);
            posY = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        Pot desiredPot = greenHouse.getSpecificPot(posX - 1, posY - 1);
        return switch (desiredPot.getPotCondition()) {
            case EMPTY -> new Result(true, "There is nothing to collect here.");
            case LOCKED -> new Result(false, "You haven't unlocked this pot yet.");
            case PLANTED -> new Result(false, "It is not ready to collect.Were you born in 6 months?");
            case COLLECTABLE -> {
                yield collectThePot(desiredPot);
            }

        };
    }


    public Result grow(String x, String y, GreenHouse greenHouse) {
        user = App.getActiveUser();
        int posX, posY;
        try {
            posX = Integer.parseInt(x);
            posY = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
        }
        Pot desiredPot = greenHouse.getSpecificPot(posX - 1, posY - 1);
        if (desiredPot.getPotCondition() != PotCondition.PLANTED) {
            return new Result(false, "You have nothing to boost here.");
        }
        if (user.getDiamond() < 1) {
            return new Result(false, "Not enough diamond for boost the growing.Get a job!");
        }
        desiredPot.setPotCondition(PotCondition.COLLECTABLE);
        return collectThePot(desiredPot);
    }
    private Result collectThePot(Pot desiredPot) {
        if (desiredPot.isItMari()) {
            user.earnCoin(500);
            desiredPot.collectPlant();
            return new Result(true, "You collected a normal Marigold.");
        } else {
            Plant collectedPlant = desiredPot.getPlantedPlant();
            if (collectedPlant == null)
                return new Result(false, "I don't know what is going on but it is not good.");
            Plant userPlant = user.getUnlockedPlants().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(collectedPlant.getName()))
                    .findFirst()
                    .orElse(null);
            if(userPlant!= null){
                userPlant.setBoosted(true);
            }
            desiredPot.collectPlant();
            return new Result(true, "You collected an unlocked plant named " + collectedPlant.getName() + ".");
        }
    }


}
