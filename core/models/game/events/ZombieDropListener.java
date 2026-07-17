package models.game.events;

import models.App;
import models.game.GameSession;
import models.greenhouse.GreenHouse;
import models.greenhouse.Pot;
import models.greenhouse.PotCondition;
import models.users.User;

import java.util.Random;

public class ZombieDropListener implements GameEventListener {
    enum ZombieDrop{
        POT,
        DIAMOND,
        COIN
    }
    @Override
    public void onEvent(GameEvent event, GameEventPayload payload) {
        if(!(event==GameEvent.ZOMBIE_KILLED_LAWN_MOWER||event==GameEvent.ZOMBIE_KILLED)) return;
        ZombieDrop type = getDropType();
        if(type == null) return;
        User user = App.getActiveUser();
        switch (type) {
            case POT -> {
                GreenHouse userGreenHouse = user.getGreenHouse();
                int cnt =0;
                for(Pot[] pots :  userGreenHouse.getPots()) {
                    for(Pot pot: pots){
                        cnt++;
                        if(pot.getPotCondition()== PotCondition.LOCKED){
                            pot.setPotCondition(PotCondition.EMPTY);
                            GameSession.notify("A zombie dropped a pot; you have "+cnt+" <coins/diamonds/pots> now.");
                        }
                    }
                }
            }
            case DIAMOND -> {
                user.earnDiamond(1);
                GameSession.notify("A zombie dropped a diamond; you have "+user.getDiamond()+" <coins/diamonds/pots> now.");
            }
            case COIN -> {
                user.earnCoin(50);
                GameSession.notify("A zombie dropped 50 coins; you have "+user.getCoin()+" <coins/diamonds/pots> now.");
            }
        }

    }


    private ZombieDrop getDropType(){
        Random rand = new Random();
        int chance = rand.nextInt(10);
        if(chance == 9){
            int choose = rand.nextInt(3);
            return switch (choose) {
                case 0 -> ZombieDrop.POT;
                case 1 -> ZombieDrop.DIAMOND;
                default -> ZombieDrop.COIN;
            };
        }else return null;
    }

}
