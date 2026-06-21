package controllers.MenuController;

import models.App;
import models.Result;
import models.database.DataBaseManager;
import models.enums.Menu;
import models.users.User;

public class MainMenuController {


    public Result logout() {
        User currentUser = App.activeUser;
        if (currentUser == null)
            return new Result(false, "no user is currently logged in");

        currentUser.setStayLoggedIn(false);
        DataBaseManager.saveOrUpdateUser(currentUser);

        App.activeUser = null;
        App.activeMenu = Menu.LOGIN_MENU;

        return new Result(true, "logged out successfully");
    }

}
