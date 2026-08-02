package com.Project.PVZ.controllers.MenuController;

import com.Project.PVZ.models.App;
import com.Project.PVZ.models.Result;
import com.Project.PVZ.models.database.DataBaseManager;
import com.Project.PVZ.models.enums.Menu;
import com.Project.PVZ.models.users.User;

public class MainMenuController {


    public Result logout() {
        User currentUser = App.getActiveUser();
        if (currentUser == null)
            return new Result(false, "no user is currently logged in");

        currentUser.setStayLoggedIn(false);

        DataBaseManager.saveOrUpdateUser(currentUser);

        currentUser.getQuestManager().unregisterFromAllEvents();
        App.setActiveUser(null);
        App.setActiveMenu(Menu.LOGIN_MENU);

        return new Result(true, "logged out successfully");
    }

}
