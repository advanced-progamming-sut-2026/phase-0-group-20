package controllers.GameController;

import models.App;
import models.Result;
import models.entities.plants.Plant;
import models.greenhouse.GreenHouse;
import models.greenhouse.Pot;
import models.greenhouse.PotCondition;
import models.users.User;
import views.GreenHouseMenu;

public class GreenHouseController {
    private final User user = App.getActiveUser();

    public Result showGreenHouse(GreenHouse greenHouse) {
        return new Result(true , greenHouse.showGreenHouse());
    }

    public Result plantPot(String x, String y, GreenHouse greenHouse) {
        int posX, posY;
        try {
            posX = Integer.parseInt(x);
            posY = Integer.parseInt(y);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid coordinate given. (Integer above ZERO)");
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
                if (desiredPot.isItMari()) {
                    user.earnCoin(500);
                    desiredPot.collectPlant();
                    yield new Result(true, "You collected a normal Marigold.");
                } else {
                    Plant collectedPlant = desiredPot.getPlantedPlant();
                    if (collectedPlant == null)
                        yield new Result(false, "I don't know what is going on but it is not good.");
                    // we should boost the plant here but it is not implemented.
                    desiredPot.collectPlant();
                    yield new Result(true, "You collected an unlocked plant named " + collectedPlant.getName() + ".");
                }
            }

        };
    }

    public Result grow(String x, String y, GreenHouse greenHouse) {
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

        return collect(x, y, greenHouse);
    }


}
