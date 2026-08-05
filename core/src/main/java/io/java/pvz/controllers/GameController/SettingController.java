package io.java.pvz.controllers.GameController;

import io.java.pvz.models.Result;
import io.java.pvz.models.Settings;

public class SettingController {

    public Result changeDifficulty(String amountStr) {
        try {
            int desiredDifficulty = Integer.parseInt(amountStr);
            if (desiredDifficulty < 1 || desiredDifficulty > 5) {
                return new Result(false, "Invalid difficulty (Must be an integer [1-5])");
            }

            Settings.getInstance().setDifficulty(desiredDifficulty);
            return new Result(true, "Difficulty changed successfully to " + desiredDifficulty);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid difficulty (Must be an integer [1-5])");
        }
    }

    public int getDifficulty() {
        return Settings.getInstance().getDifficulty();
    }

    public void setDifficulty(int difficulty) {
        if(difficulty >= 1 && difficulty <= 5) {
            Settings.getInstance().setDifficulty(difficulty);
        }
    }

    public float getMusicVolume() {
        return Settings.getInstance().getMusicVolume();
    }

    public void setMusicVolume(float volume) {
        Settings.getInstance().setMusicVolume(volume);
    }

    public float getSfxVolume() {
        return Settings.getInstance().getSfxVolume();
    }

    public void setSfxVolume(float volume) {
        Settings.getInstance().setSfxVolume(volume);
    }

    public boolean isGrid() {
        return Settings.getInstance().isGrid();
    }

    public void setGrid(boolean grid) {
        Settings.getInstance().setGrid(grid);
    }

    public boolean isDebug() {
        return Settings.getInstance().isDebug();
    }

    public void setDebug(boolean debug) {
        Settings.getInstance().setDebug(debug);
    }
}
