package io.java.pvz.controllers.MenuController;

import io.java.pvz.models.App;
import io.java.pvz.models.Result;
import io.java.pvz.models.database.DataBaseManager;
import io.java.pvz.models.enums.Menu;
import io.java.pvz.models.users.User;

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
