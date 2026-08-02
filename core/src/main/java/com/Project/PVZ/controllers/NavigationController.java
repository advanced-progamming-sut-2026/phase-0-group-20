package com.Project.PVZ.controllers;

import com.Project.PVZ.models.App;
import com.Project.PVZ.models.Result;
import com.Project.PVZ.models.database.DataBaseManager;
import com.Project.PVZ.models.enums.Menu;
import com.Project.PVZ.models.game.GameSession;


public final class NavigationController {

    private NavigationController() {
    }

    public static Result enterMenu(String targetMenuName) {
        Menu target = Menu.getByName(targetMenuName);
        if (target == null)
            return new Result(false, "no such menu exists");

        Menu current = App.getActiveMenu();

        if (current.getAllowedEntryTargets() == null || !current.getAllowedEntryTargets().contains(target))
            return new Result(false, "you cannot enter this menu from here");

        App.setActiveMenu(target);

        return new Result(true, "entered " + target.getName());
    }

    public static Result exitMenu() {
        Menu current = App.getActiveMenu();
        if(current == Menu.GAME_FLOW_MENU&& GameSession.getInstance().getCurrentChapter().getCurrentLevel()!=null){
            GameSession.getInstance().getCurrentChapter().getCurrentLevel().destroyLevelFields();
        }
        if (current == Menu.MAIN_MENU)
            return new Result(false, "use the logout command to exit the main menu");

        Menu target = current.getExitTarget();
        if (target == null) {
            System.exit(0);
        }

        App.setActiveMenu(target);
        if(target != Menu.SIGNUP_MENU && target != Menu.LOGIN_MENU){
            DataBaseManager.saveOrUpdateUser(App.getActiveUser());
        }
        return new Result(true, "exited to " + target.getName());
    }

    public static Result showCurrentMenu() {
        return new Result(true, App.getActiveMenu().getName());
    }


}
