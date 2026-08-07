package io.java.pvz.controllers.GameController;

import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.entities.plants.Plant;
import io.java.pvz.models.game.events.GameEvent;
import io.java.pvz.models.game.events.GameEventMessenger;
import io.java.pvz.models.game.events.GameEventPayload;
import io.java.pvz.models.greenhouse.GreenHouse;
import io.java.pvz.models.greenhouse.Pot;
import io.java.pvz.models.greenhouse.PotCondition;
import io.java.pvz.models.users.User;

public class GreenHouseController {
    private User user;

    public Result showGreenHouse(GreenHouse greenHouse) {
        return new Result(true, greenHouse.showGreenHouse());
    }

    public Result plantPot(int x, int y, GreenHouse greenHouse) {
        user = App.getActiveUser();
        int posX = x;
        int posY = y;
        Pot desiredPot = greenHouse.getSpecificPot(posX , posY );
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

    public Result collect(int x, int y, GreenHouse greenHouse) {
        Pot desiredPot = greenHouse.getSpecificPot(x, y);
        if(desiredPot.getPotCondition() == PotCondition.COLLECTABLE){
            collectThePot(desiredPot);
            return new Result(true , "You successfully collected a plant here.");
        }
        return new Result(false, "You failed to collect a plant here.");

    }


    public Result grow(int x , int y , GreenHouse greenHouse) {
        user = App.getActiveUser();
        int posX = x;
        int posY = y;
        Pot desiredPot = greenHouse.getSpecificPot(posX , posY );
        if (desiredPot.getPotCondition() != PotCondition.PLANTED) {
            return new Result(false, "You have nothing to boost here.");
        }
        if (user.getDiamond() < 10) {
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                    .message("Not Enough Diamond \uD83D\uDC8E")
                    .build());
            return new Result(false, "Not enough diamond for boost the growing.Get a job!");
        }
        user.costDiamond(10);
        desiredPot.setPotCondition(PotCondition.COLLECTABLE);
        return new Result(true,"success");
    }

    public void buyPot(Pot pot , int price){
        User user = App.getActiveUser();
        if (user.getDiamond() < 10) {
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                    .message("Not Enough Diamond \uD83D\uDC8E")
                    .build());
            return;

        }
        user.costDiamond(10);
        pot.setPotCondition(PotCondition.EMPTY);
    }


    private Result collectThePot(Pot desiredPot) {
        if (desiredPot.isItMari()) {
            user.earnCoin(500);
            desiredPot.collectPlant();
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                    .message("You collected 500 coins")
                    .build());
            return new Result(true, "You collected a normal Marigold.");
        } else {
            Plant collectedPlant = desiredPot.getPlantedPlant();
            if (collectedPlant == null)
                return new Result(false, "I don't know what is going on but it is not good.");
            Plant userPlant = user.getUnlockedPlants().stream()
                    .filter(p -> p.getName().equalsIgnoreCase(collectedPlant.getName()))
                    .findFirst()
                    .orElse(null);
            if (userPlant != null) {
                userPlant.setBoosted(true);
            }
            desiredPot.collectPlant();
            GameEventMessenger.getInstance().dispatch(GameEvent.NOTIFY,
                new GameEventPayload.Builder(GameEvent.NOTIFY)
                    .message(collectedPlant.getName()+" is BOOSTED FOR THE NEXT MATCH")
                    .build());
            return new Result(true, "You collected an unlocked plant named " + collectedPlant.getName() + ".");
        }
    }


}
