package models.greenhouse;

import models.App;
import models.entities.plants.Plant;
import models.timeManager.Ticker;

import java.util.List;
import java.util.Random;

public class Pot implements Ticker {
    private PotCondition potCondition;
    private Plant plantedPlant = null;
    private int remainedTimeToCollect;
    private boolean isItMari = false;
    private static final int TIME_TO_HATCH_MARIGOLD = 72000;
    private static final int TIME_TO_HATCH_PLANT = 288000;
    public Pot(){
        potCondition = PotCondition.EMPTY;
    }

        @Override
    public void onTick(int currentTick) {
        if(currentTick> 0){
            currentTick--;
            if(currentTick==0){
                potCondition = PotCondition.COLLECTABLE;
            }
        }
    }

    public void plantPlant() {
        isItMari = getTheType();
        if(isItMari){
            remainedTimeToCollect = TIME_TO_HATCH_MARIGOLD;
            potCondition = PotCondition.PLANTED;
        }
        else{
            Plant plant = getRandomUnlockedPlant();
            plantedPlant = plant;
            remainedTimeToCollect = TIME_TO_HATCH_PLANT;
            potCondition = PotCondition.PLANTED;
        }
    }

    public void collectPlant() {
        this.potCondition = PotCondition.EMPTY;
        this.plantedPlant = null;
        this.isItMari = false;
    }

    public void growFaster(int x, int y) {

    }


    public PotCondition getPotCondition() {
        return potCondition;
    }

    public void setPotCondition(PotCondition potCondition) {
        this.potCondition = potCondition;
    }

    public Plant getPlantedPlant() {
        return plantedPlant;
    }

    public void setPlantedPlant() {

    }

    public int getRemainedTimeToCollect() {
        return remainedTimeToCollect;
    }

    public void setRemainedTimeToCollect(int remainedTimeToCollect) {
        this.remainedTimeToCollect = remainedTimeToCollect;
    }

    public boolean isItMari() {
        return isItMari;
    }

    public void setItMari(boolean itMari) {
        isItMari = itMari;
    }

    private boolean getTheType(){
        Random rand = new Random();
        int chance = rand.nextInt(100);
        if(chance<50){
            return true;
        }else{
            return false;
        }
    }

    private Plant getRandomUnlockedPlant(){
        List<Plant> plants = App.getActiveUser().getUnlockedPlants();
        Random rand = new Random();
        int index =  rand.nextInt(plants.size());
        return plants.get(index);
    }


}
