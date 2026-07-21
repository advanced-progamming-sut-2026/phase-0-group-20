package controllers.GameController;

import models.App;
import models.Result;

public class SettingController {

    public Result changeDifficulty(String amountStr) {
        try {
            int desiredDifficulty = Integer.parseInt(amountStr);
            if (desiredDifficulty < 1 || desiredDifficulty > 5)
                return new Result(false, "Invalid difficulty (Must be an integer [1-5])");
            App.changeDifficulty(desiredDifficulty);
            return new Result(true, "Difficulty changed successfully to " + desiredDifficulty);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid difficulty (Must be an integer [1-5])");
        }
    }

}
