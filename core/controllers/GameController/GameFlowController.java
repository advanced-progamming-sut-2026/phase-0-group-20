package controllers.GameController;

import models.Result;
import models.game.GameSession;

public class GameFlowController {

    public Result advanceTime(String amount){
        int timeAmount= 0;
        try{
            timeAmount = Integer.parseInt(amount);
        }
        catch(NumberFormatException e){
            return new Result(false, "Invalid amount given. (Integer above ZERO)");
        }
        if(timeAmount <= 0){
            return new Result(false, "Even Dr.Strange couldn't travel to the past.\nwho the are you?");
        }
        GameSession.getInstance().update(timeAmount);
        return new Result(true, "Successfully advanced time for "+timeAmount+" seconds.");
    }

    public Result collectSun(String x, String y) {
        return null;
    }

    public Result cheatAddSun(String amount) {

        return null;
    }

    public Result releaseNuke(){
        return null;
    }
    public Result showSunAmount() {
        return null;
    }

    public Result plantPlant(String plantName,String x,String y) {
        return null;
    }

    public Result cheatRemoveCooldown() {
        return null;
    }

    public Result pluckPlant(String x, String y) {
        return null;
    }

    public Result feedPlant(String x, String y) {
        return null;
    }

    public Result cheatAddPlantFood(){
        return null;
    }

    public Result showMap() {
        return null;
    }

    public Result showPlantsStatus() {
        return null;
    }

    public Result showTileStatus(String x , String y) {
        return null;
    }

}
