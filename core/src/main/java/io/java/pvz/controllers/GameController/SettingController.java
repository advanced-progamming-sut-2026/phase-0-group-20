package io.java.pvz.controllers.GameController;

import io.java.pvz.controllers.AudioManager;
import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.Settings;

public class SettingController {

    public Result changeDifficulty(String amountStr) {
        try {
            int desiredDifficulty = Integer.parseInt(amountStr);
            if (desiredDifficulty < 1 || desiredDifficulty > 5) {
                return new Result(false, "Invalid difficulty (Must be an integer [1-5])");
            }

            App.getSettings().setDifficulty(desiredDifficulty);
            return new Result(true, "Difficulty changed successfully to " + desiredDifficulty);
        } catch (NumberFormatException e) {
            return new Result(false, "Invalid difficulty (Must be an integer [1-5])");
        }
    }

    public int getDifficulty() {
        return App.getSettings().getDifficulty();
    }

    public void setDifficulty(int difficulty) {
        if(difficulty >= 1 && difficulty <= 5) {
            App.getSettings().setDifficulty(difficulty);
        }
    }

    public float getMusicVolume() {
        return App.getSettings().getMusicVolume();
    }

    public void setMusicVolume(float volume) {
        App.getSettings().setMusicVolume(volume);
        AudioManager.getInstance().changeMusicVolume(volume);
    }

    public float getSfxVolume() {
        return App.getSettings().getSfxVolume();
    }

    public void setSfxVolume(float volume) {
        App.getSettings().setSfxVolume(volume);
        AudioManager.getInstance().changeSfxVolume(volume);
    }

    public boolean isGrid() {
        return App.getSettings().isGrid();
    }

    public void setGrid(boolean grid) {
        App.getSettings().setGrid(grid);
    }

    public boolean isDebug() {
        return App.getSettings().isDebug();
    }

    public void setDebug(boolean debug) {
        App.getSettings().setDebug(debug);
    }
}
